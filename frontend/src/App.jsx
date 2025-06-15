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
  const [currency, setCurrency] = useState('USD');
  const isValidDate = (date) => {
    return date instanceof Date && !isNaN(date);
  };
  
  const Pagination = ({ data, itemsPage=10}) =>{
    const [currentPage, setCurrentPage] = useState(1);
    const totalPages = Math.ceil(data.length / itemsPage);
    
    const handlePageChange = (page) => {
      setCurrentPage(page);
    };
  
    const startIndex = (currentPage - 1) * itemsPage;
    const endIndex = startIndex + itemsPage;
    const currentData = data.slice(startIndex, endIndex);
  
    return (
      <div>
        <table>
          <thead>
            <tr>
              <th>Date</th>
              <th>Price({currency})</th>
              <th>Marker</th>
            </tr>
          </thead>
          <tbody>
            {currentData.map((p) => (
              <tr key={p.date} style={{ background: p.marker === 'high' ? '#f72525' : p.marker === 'low' ? '#f09554' : '' }}>
                <td>{p.date}</td>
                <td>{p.price.toFixed(2)}</td>
                <td>{p.marker}</td>
              </tr>
            ))}
          </tbody>
        </table>
        <div style={{ marginTop: 10 }}>
          {Array.from({ length: totalPages }, (_, i) => (
            <button
              key={i + 1}
              onClick={() => handlePageChange(i + 1)}
              style={{ margin: '0 5px', backgroundColor: currentPage === i + 1 ? '#007bff' : '#f0f0f0', color: currentPage === i + 1 ? '#fff' : '#000' }}
            >
              {i + 1}
            </button>
          ))}
        </div>
      </div>
    );

  }
  const sortedPrices = [
    ...prices.filter(p=> p.marker === 'high'),
    ...prices.filter(p=> p.marker === 'low'),
    ...prices.filter(p=> p.marker !== 'high' && p.marker !== 'low')
  ];
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
    <div style={{ padding: 90 }}>
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
        <select value={currency} onChange={e => setCurrency(e.target.value)}>
          <option value="USD">USD</option>
          <option value="EUR">EUR</option>
          <option value="GBP">GBP</option>
          <option value="INR">INR</option>
          </select>
        <button onClick={fetchPrices}>Fetch</button>
      </div>
      {error && (
        <div style={{ color: 'red', marginTop: 10 }}>{error}</div>
      )}
      {prices.length > 0 && (
        <div style={{ marginTop: 20 }}>
          <h2>Prices from {start.toLocaleDateString()} to {end.toLocaleDateString()}</h2>
          <Pagination data={sortedPrices} itemsPage={10} />
        </div>
      )}
      {prices.length === 0 && !error && (
        <div style={{ marginTop: 20 }}>No prices available for the selected date range.</div>
      )}
      <div style={{ marginTop: 20 }}>
        <p>Note: Prices marked in red are high, orange are low, and others are normal.</p>
    </div>
    </div>
  );
}