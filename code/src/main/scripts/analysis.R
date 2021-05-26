# Compute the probabilistic score of the axioms contained in data frame d:
bls = function(d)
{
  delta <- 1.96*sqrt((d$conf + 2)*(d$refc - d$conf + 2))/(d$refc + 4)
  left <- pmax(0, (d$conf + 2)/(d$refc + 4) - delta)
  right <- pmin(1, (d$conf + 2)/(d$refc + 4) + delta)
  (left + right)/2
}

setEPS()
magnification = 1.666666666666

# Read the table with the statistics produced by RDF Miner for the exhaustive dbo axioms experiment
 d <- read.table("table1.txt", header = TRUE)

# Generate the figures:

postscript("docs/EKAW2014/time-hist.eps")
hist(d$time/60000, xlab = "Elapsed Time (min)", ylab = "Number of Axioms", main = "", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm docs/EKAW2014/time-hist.pdf")

postscript("docs/EKAW2014/time-refc.eps")
plot(d$refc, d$time/60000, xlab = "Reference Cardinality", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm docs/EKAW2014/time-refc.pdf")


# Read the table with the statistics produced by RDF Miner for the explorative systematic experiment
 d <- read.table("table.txt", header = TRUE)
# Compute the ARI for all axioms:
d$ari <- d$nec + d$poss - 1
# Compute Bühmann and Lehmann's score:
d$bls <- bls(d)

# Generate the figures:

#postscript("docs/EKAW2014/systematic-time-refc.eps")
#plot(d$refc, d$time/60000, xlab = "Reference Cardinality", ylab = "Elapsed Time (min)")
#dev.off()
#system("rm docs/EKAW2014/systematic-time-refc.pdf")

postscript("docs/EKAW2014/systematic-time-hist.eps")
hist(d$time/60000, xlab = "Elapsed Time (min)", ylab = "Number of Axioms", main = "", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm docs/EKAW2014/systematic-time-hist.pdf")

postscript("docs/EKAW2014/time-ARI.eps")
plot(d$poss + d$nec - 1, d$time/60000, xlab = "Acceptance/Rejection Index", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm docs/EKAW2014/time-ARI.pdf")

postscript("docs/EKAW2014/ARI-hist.eps")
hist(d$poss + d$nec - 1, xlab = "Acceptance/Rejection Index", main = "", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm docs/EKAW2014/ARI-hist.pdf")

postscript("docs/EKAW2014/ARI-BLS.eps")
plot(d$bls ~ d$ari, xlab = "Acceptance/Rejection Index", ylab = "Bühmann and Lehmann's Score", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm docs/EKAW2014/ARI-BLS.pdf")

# Save the data frame to a file:
write.table(d, "axiom-statistics.txt", row.names = FALSE)

