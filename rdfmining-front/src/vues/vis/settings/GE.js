export const options = {
    // maintainAspectRatio: false,
    scales: {
        y: {
            beginAtZero: true
        },
        x: {
            beginAtZero: false,
            type: 'linear',
            title: {
                display: true,
                text: 'Generation',
                font: {
                    size: 18
                }
            },
        }
    },
    plugins: {
        legend: {
            display: true,
            labels: {
                font: {
                    size: 16,
                }
            }
        },
        tooltip: {
            callbacks: {
                title: function (context) {
                    return "Generation " + context[0].label;
                },
                label: function (context) {
                    return context.formattedValue + " ms.";
                },
            }
        }
    }
};