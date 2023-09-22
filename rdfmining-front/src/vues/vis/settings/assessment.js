export const options = {
    scales: {
        x: {
            display: false,
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
                    return "Phenotype: " + context[0].label;
                },
                label: function (context) {
                    return context.formattedValue + " ms.";
                },
            }
        }
    }
};