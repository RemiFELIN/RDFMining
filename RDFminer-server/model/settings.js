class Settings {

    constructor(data) {
        this.bnf = data.bnf;
        this.populationSize = data.populationSize;
        this.stopCriterionType = data.stopCriterionType;
        this.time = data.time;
        this.effort = data.effort;
        this.sizeChromosome = data.sizeChromosome;
        this.maxWrap = data.maxWrap;
        this.eliteSelectionRate = data.eliteSelectionRate;
        this.selectionType = data.selectionType;
        this.selectionRate = data.selectionRate;
        this.tournamentSelectionRate = data.tournamentSelectionRate;
        this.mutationType = data.mutationType;
        this.mutationRate = data.mutationRate;
        this.crossoverType = data.crossoverType;
        this.crossoverRate = data.crossoverRate;
        this.noveltySearch = data.noveltySearch;
        this.crowding = data.crowding;
        this.shaclAlpha = data.shaclAlpha;
        this.shaclProb = data.shaclProb;
        this.axioms = data.axioms;
        this.shapes = data.shapes;
    }

}

module.exports = Settings;