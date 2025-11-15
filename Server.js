require('dotenv').config();
const express = require('express');
const bodyParser = require('body-parser');
const mysql = require('mysql2/promise');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const cors = require('cors');

const app = express();
app.use(bodyParser.json());
app.use(cors());

const PORT = process.env.PORT || 3000;
const JWT_SECRET = process.env.JWT_SECRET || 'secret';

// create pool
const pool = mysql.createPool({
  host: process.env.DB_HOST || 'localhost',
  user: process.env.DB_USER || 'root',
  password: process.env.DB_PASSWORD || '',
  database: process.env.DB_NAME || 'flight_app',
  waitForConnections: true,
  connectionLimit: 10,
  queueLimit: 0
});

// helper: authenticate middleware
function authenticateToken(req, res, next) {
  const authHeader = req.headers['authorization'];
  if (!authHeader) return res.status(401).json({ error: 'Missing token' });
  const token = authHeader.split(' ')[1];
  if (!token) return res.status(401).json({ error: 'Invalid token' });
  jwt.verify(token, JWT_SECRET, (err, user) => {
    if (err) return res.status(403).json({ error: 'Invalid token' });
    req.user = user;
    next();
  });
}

// register
app.post('/api/register', async (req, res) => {
  try {
    const { name, email, password } = req.body;
    if (!email || !password || !name) return res.status(400).json({ error: 'Missing fields' });
    const [rows] = await pool.execute('SELECT id FROM users WHERE email = ?', [email]);
    if (rows.length > 0) return res.status(409).json({ error: 'Email already exists' });
    const hash = await bcrypt.hash(password, 10);
    const [result] = await pool.execute('INSERT INTO users (name,email,password_hash) VALUES (?,?,?)', [name, email, hash]);
    res.json({ success: true, userId: result.insertId });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// login
app.post('/api/login', async (req, res) => {
  try {
    const { email, password } = req.body;
    if (!email || !password) return res.status(400).json({ error: 'Missing fields' });
    const [rows] = await pool.execute('SELECT id, name, password_hash FROM users WHERE email = ?', [email]);
    if (rows.length === 0) return res.status(401).json({ error: 'Invalid credentials' });
    const user = rows[0];
    const valid = await bcrypt.compare(password, user.password_hash);
    if (!valid) return res.status(401).json({ error: 'Invalid credentials' });
    const token = jwt.sign({ id: user.id, name: user.name, email }, JWT_SECRET, { expiresIn: '12h' });
    res.json({ token, user: { id: user.id, name: user.name, email } });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// search flights
app.get('/api/searchFlights', async (req, res) => {
  try {
    const { src, dest, date } = req.query;
    // basic validation
    if (!src || !dest || !date) {
      return res.status(400).json({ error: 'src, dest and date required' });
    }
    // search by date (match DATE portion of depart_datetime)
    const q = `SELECT id, airline, flight_number, src, dest, depart_datetime, arrive_datetime, price, seats_available 
               FROM flights WHERE src = ? AND dest = ? AND DATE(depart_datetime) = ? AND seats_available > 0`;
    const [rows] = await pool.execute(q, [src, dest, date]);
    res.json({ flights: rows });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// book flight (authenticated)
app.post('/api/bookFlight', authenticateToken, async (req, res) => {
  try {
    const userId = req.user.id;
    const { flight_id, seats } = req.body;
    if (!flight_id || !seats || seats <= 0) return res.status(400).json({ error: 'Invalid request' });

    const conn = await pool.getConnection();
    try {
      await conn.beginTransaction();
      // check flight
      const [frows] = await conn.execute('SELECT price, seats_available FROM flights WHERE id = ? FOR UPDATE', [flight_id]);
      if (frows.length === 0) {
        await conn.rollback();
        return res.status(404).json({ error: 'Flight not found' });
      }
      const flight = frows[0];
      if (flight.seats_available < seats) {
        await conn.rollback();
        return res.status(400).json({ error: 'Not enough seats' });
      }
      const total = parseFloat(flight.price) * seats;
      // insert booking
      const [bres] = await conn.execute('INSERT INTO bookings (user_id, flight_id, seats, total_price) VALUES (?,?,?,?)', [userId, flight_id, seats, total]);
      // decrement seats
      await conn.execute('UPDATE flights SET seats_available = seats_available - ? WHERE id = ?', [seats, flight_id]);
      await conn.commit();
      res.json({ success: true, bookingId: bres.insertId, total_price: total });
    } catch (innerErr) {
      await conn.rollback();
      throw innerErr;
    } finally {
      conn.release();
    }
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

// get my bookings
app.get('/api/myBookings', authenticateToken, async (req, res) => {
  try {
    const uid = req.user.id;
    const q = `SELECT b.id, b.seats, b.total_price, b.booking_time, f.airline, f.flight_number, f.src, f.dest, f.depart_datetime, f.arrive_datetime
               FROM bookings b JOIN flights f ON b.flight_id = f.id WHERE b.user_id = ? ORDER BY b.booking_time DESC`;
    const [rows] = await pool.execute(q, [uid]);
    res.json({ bookings: rows });
  } catch (err) {
    console.error(err);
    res.status(500).json({ error: 'Server error' });
  }
});

app.listen(PORT, () => {
  console.log(`Flight API running on port ${PORT}`);
});
