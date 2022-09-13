####################################################
# R script for the analysis of RDFMiner's results
# (c) 2014-2015 Andrea G. B. Tettamanzi
####################################################

# Compute Bühmann and Lehmann's probabilistic score of the axioms contained in data frame d:
bls = function(d)
{
  delta <- 1.96*sqrt((d$conf + 2)*(d$refc - d$conf + 2))/(d$refc + 4)
  left <- pmax(0, (d$conf + 2)/(d$refc + 4) - delta)
  right <- pmin(1, (d$conf + 2)/(d$refc + 4) + delta)
  (left + right)/2
}

setEPS()
magnification = 1.666666666666

# Read the table with the statistics produced by RDF Miner w/o time capping
d <- read.table("../../table.txt", header = TRUE)
# Read the table with the statistics produced by RDF Miner with dynamic time capping
dtc <- read.table("../../dtc-table.txt", header = TRUE)
# Read the time predictors calculated for the classes of DBpedia
tp <- read.table("tp.txt", header = TRUE, comment.char = "")

# Compute the ARI for all axioms:
d$ari <- d$nec + d$poss - 1
dtc$ari <- dtc$nec + dtc$poss - 1
# Compute Bühmann and Lehmann's score:
d$bls <- bls(d)
dtc$bls <- bls(dtc)
# Compute the number of classes intersecting the subclass
d$ncis <- 0
for(i in 1:length(d$axiom))
  d$ncis[i] <- length(grep(strsplit(as.character(d$axiom[i]), " ")[[1]][1], d$axiom, fixed = TRUE))
dtc$ncis <- 0
for(i in 1:length(dtc$axiom))
  dtc$ncis[i] <- length(grep(strsplit(as.character(dtc$axiom[i]), " ")[[1]][1], dtc$axiom, fixed = TRUE))
# Compute the time predictor
d$tp <- d$refc*d$ncis
dtc$tp <- dtc$refc*dtc$ncis

# Generate the figures:

postscript("tp.eps")
plot(1:dim(tp)[1], tp$tp, type = "l", log = "y", ylab = "Time Predictor", xlab = "Class Rank", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm tp.pdf", ignore.stderr = TRUE, wait = FALSE)

postscript("time-tp.eps")
plot(d$tp, d$time/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm time-tp.pdf", ignore.stderr = TRUE, wait = FALSE)

# Gather some statistics about the time it took to test the accepted axioms:
b <- boxplot(d$time[d$ari>1/3]/60000 ~ d$tp[d$ari>1/3], plot = FALSE)
time <- b$stats[5, ] # get the maximum time for each group
gtp <- as.numeric(b$names) # get the tp value for each group

postscript("time-tp-acc.eps")
plot(d$tp[d$ari>1/3], d$time[d$ari>1/3]/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
# Add a dashed linear trend line and a dotted time-out line:
# fit <- glm(d$time[d$ari>1/3]/60000 ~ d$tp[d$ari>1/3])
# abline(fit, lty = 2) # the trend line
# abline(a = 2, b = 2*fit$coefficients[2], lty = 3) # the time-out line: 2 min + 2*b*tp
fit.max <- glm(time ~ gtp)
abline(fit.max, lty = 2) # the trend line, computed on the maxima for each group
abline(a = 2, b = fit.max$coefficients[2], lty = 3) # the relevant time out line: 2 min + b*tp
text(0, 25, sprintf("b = %g", fit.max$coefficients[2]), pos = 4, cex = magnification)
text(0, 23, "a = 2", pos = 4, cex = magnification)
# text(0, 23, sprintf("ball = %g", fit$coefficients[2]), pos = 4, cex = magnification)
dev.off()
system("rm time-tp-acc.pdf", ignore.stderr = TRUE, wait = FALSE)

postscript("time-tp-rej.eps")
plot(d$tp[d$ari<=1/3], d$time[d$ari<=1/3]/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
dev.off()
system("rm time-tp-rej.pdf", ignore.stderr = TRUE, wait = FALSE)

postscript("time-ARI.eps")
plot(d$ari, d$time/60000, xlab = "Acceptance/Rejection Index", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
abline(v = c(1/3), col="red")
dev.off()
system("rm time-ARI.pdf", ignore.stderr = TRUE, wait = FALSE)

postscript("ratio-ARI.eps")
color.scale <- terrain.colors(100)
plot(d$ari, d$time/(d$tp*60000), col = color.scale[1 + 99*log(d$time)/log(max(d$time))], log = "y", xlab = "Acceptance/Rejection Index", ylab = "Elapsed Time to Time Predictor Ratio", cex.lab = magnification, cex.axis = magnification)
abline(v = c(1/3), col="red")
abline(h = c(fit.max$coefficients[2]), lty = 2)
dev.off()
system("rm ratio-ARI.pdf", ignore.stderr = TRUE, wait = FALSE)

# For a given value of the time predictor, find its rank
tp.rank = function(tpvalue)
{
  min(which(tp$tp>=tpvalue))
}
tp.ranks = Vectorize(tp.rank)

postscript("pred-and-actual-time.eps")
time.cap = 2 + round(fit.max$coefficients[2]*tp$tp)
plot(1:dim(tp)[1], time.cap, type = "l", log = "y", ylab = "Time (min)", xlab = "Class Rank", cex.lab = magnification, cex.axis = magnification, ylim = c(min(dtc$time)/60000, max(time.cap)))
points(1:dim(tp)[1], fit.max$coefficients[2]*tp$tp, type = "l", lty = 2, col = "gray")
points(tp.ranks(dtc$tp[d$ari>1/3]), dtc$time[d$ari>1/3]/60000, pch = ".", col = "green")
points(tp.ranks(dtc$tp[d$ari<=1/3]), dtc$time[d$ari<=1/3]/60000, pch = ".", col = "red")
dev.off()
system("rm pred-and-actual-time.pdf", ignore.stderr = TRUE, wait = FALSE)

postscript("ARI-BLS.eps")
color.scale <- terrain.colors(100)
plot(d$bls ~ d$ari, col = color.scale[1 + 99*log(d$time)/log(max(d$time))], xlab = "Acceptance/Rejection Index", ylab = "Bühmann and Lehmann's Score", cex.lab = magnification, cex.axis = magnification)
abline(h = c(.7), col="blue")
abline(v = c(1/3), col="red")
dev.off()
system("rm ARI-BLS.pdf", ignore.stderr = TRUE, wait = FALSE)

postscript("ARI-BLS-dtc.eps")
color.scale <- terrain.colors(100)
plot(dtc$bls ~ dtc$ari, col = color.scale[1 + 99*log(dtc$time)/log(max(dtc$time))], xlab = "Acceptance/Rejection Index", ylab = "Bühmann and Lehmann's Score", cex.lab = magnification, cex.axis = magnification)
abline(h = c(.7), col="blue")
abline(v = c(1/3), col="red")
dev.off()
system("rm ARI-BLS-dtc.pdf", ignore.stderr = TRUE, wait = FALSE)

# Save the data frames to a file:
write.table(d, "axiom-statistics.txt", row.names = FALSE)
write.table(dtc, "dtc-axiom-statistics.txt", row.names = FALSE)

