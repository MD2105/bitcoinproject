import { useState } from 'react';
import axios from 'axios';
import DatePicker from 'react-datepicker';
import 'react-datepicker/dist/react-datepicker.css';
export default function App() {
  const [start, setStart] = useState(new Date());
  const [end, setEnd] = useState(new Date());
  const [offline, setOffline] = useState(false);
  const [prices, setPrices] = useState([]);
  const [error, setError] = useState('');
  const isValidDate = (date) => {
    return date instanceof Date && !isNaN(date);
  };
   

  const fetchPrices = async () => {
    setError('');
 
    
    if (!isValidDate(start) || !isValidDate(end)) {
      setError('Please select valid start and end dates.');
      return;
    }
    if (start > end) {
      setError('Start date must be before or equal to end date.');
      return;
    }
    const s = start.toISOString().slice(0, 10);
    const e = end.toISOString().slice(0, 10);
    try {
      const res = await axios.get(
        `http://localhost:8080/api/v1/prices?start=${s}&end=${e}&offline=${offline}`
      );
      setPrices(res.data);
    } catch (err) {
      console.error('Error fetching prices:', err);
      let msg = err.response?.data?.message || 'Failed to fetch prices. Please check your input or try again later.';
      if (msg === 'Public API not available') {
        msg = 'Bitcoin price service is temporarily unavailable. Please try again later or use offline mode if available.';
      }
      setError(msg);
    }
  };


  return (
    <div style={{ padding: 20 }}>
      <h1>Bitcoin Price Viewer</h1>
      <div>
        <DatePicker selected={start} onChange={d => setStart(d)} />
        <DatePicker selected={end} onChange={d => setEnd(d)} />
        <label>
          <input
            type="checkbox"
            checked={offline}
            onChange={e => setOffline(e.target.checked)}
          />
          Offline Mode
        </label>
        <button onClick={fetchPrices}>Fetch</button>
      </div>
      {error && (
        <div style={{ color: 'red', marginTop: 10 }}>{error}</div>
      )}
      <table border={1} cellPadding={5} style={{ marginTop: 20 }}>
        <thead>
          <tr>
            <th>Date</th>
            <th>Price</th>
            <th>Marker</th>
          </tr>
        </thead>
        <tbody>
          {prices.map(p => (
            <tr key={p.date} style={{ background: p.marker === 'high' ? '#c8e6c9' : p.marker === 'low' ? '#ffcdd2' : '' }}>
              <td>{p.date}</td>
              <td>{p.price.toFixed(2)}</td>
              <td>{p.marker}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}