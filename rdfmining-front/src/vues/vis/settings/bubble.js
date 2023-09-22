export const bubbleOptions = {
    titleTextStyle: { fontSize: 30 },
    backgroundColor: "#ffffff",
    hAxis: {
        title: "# exceptions",
        textStyle: { fontSize: 30 },
        titleTextStyle: { fontSize: 20, italic: false, bold: true }
    },
    vAxis: {
        title: "CPU computation time (ms.)",
        textStyle: { fontSize: 30 },
        titleTextStyle: { fontSize: 20, italic: false, bold: true }
    },
    colorAxis: {
        minValue: 0,
        maxValue: 1,
        colors: ['#00FF00', '#FF0000']
    },
    bubble: { textStyle: { auraColor: 'none', fontSize: 1 } },
    sizeAxis: { minSize: 20, maxSize: 100 },
    explorer: {},
};

export const headers = [ "phenotype", "numExceptions", "elapsedTime", "Violations Ratio", "referenceCardinality" ];