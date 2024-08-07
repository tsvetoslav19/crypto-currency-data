document.addEventListener('DOMContentLoaded', function() {
    const symbolElement = document.getElementById('symbol');
    const intervalElement = document.getElementById('interval');
    const smoothElement = document.getElementById('smooth');
    let chart; // Variable to store the chart instance

    function fetchChartData() {
        const symbol = symbolElement.value;
        const interval = intervalElement.value;
        const smooth = smoothElement.checked;

        fetch(`/api/history?symbol=${symbol}&interval=${interval}`)
            .then(response => response.json())
            .then(data => {
                if (smooth) {
                    data = smoothData(data);
                }
                renderChart(data);
            });
    }

    function renderChart(data) {
        // Clear the existing chart container
        const chartContainer = document.getElementById('tvchart');
        chartContainer.innerHTML = '';

        // Create a new chart instance
        chart = LightweightCharts.createChart(chartContainer, {
            width: 1500,
            height: 600,
        });

        const lineSeries = chart.addLineSeries();
        const chartData = data.map(item => ({
            time: new Date(item.timestamp).getTime() / 1000,
            value: item.price,
        }));

        lineSeries.setData(chartData);
    }

    function smoothData(data) {
        // Smoothing algorithm, moving average
        const smoothedData = [];
        const period = 5; // Number of data points for moving average

        for (let i = 0; i < data.length; i++) {
            if (i < period - 1) {
                smoothedData.push(data[i]);
            } else {
                const sum = data.slice(i - period + 1, i + 1).reduce((acc, val) => acc + val.price, 0);
                const average = sum / period;
                smoothedData.push({ ...data[i], price: average });
            }
        }

        return smoothedData;
    }

    symbolElement.addEventListener('change', fetchChartData);
    intervalElement.addEventListener('change', fetchChartData);
    smoothElement.addEventListener('change', fetchChartData);

    // Initial fetch
    fetchChartData();
});
