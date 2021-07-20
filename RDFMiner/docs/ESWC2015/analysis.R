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

# Read the table with the statistics produced by RDF Miner w/o time capping
d <- read.table("../../table.txt", header = TRUE)
# Read the table with the statistics produced by RDF Miner with time capping
d20 <- rbind(
  read.table("../../table-20_1-9.txt", header = TRUE),
  read.table("../../table-20_11-19.txt", header = TRUE),
  read.table("../../table-20.txt", header = TRUE)
)

# Compute the ARI for all axioms:
d$ari <- d$nec + d$poss - 1
d20$ari <- d20$nec + d20$poss - 1
# Compute Bühmann and Lehmann's score:
d$bls <- bls(d)
d20$bls <- bls(d20)
# Compute the number of classes intersecting the subclass
d$ncis <- 0
for(i in 1:length(d$axiom))
  d$ncis[i] <- length(grep(strsplit(as.character(d$axiom[i]), " ")[[1]][1], d$axiom, fixed = TRUE))
d20$ncis <- 0
for(i in 1:length(d20$axiom))
  d20$ncis[i] <- length(grep(strsplit(as.character(d20$axiom[i]), " ")[[1]][1], d20$axiom, fixed = TRUE))
# Compute the time predictor
d$tp <- d$refc*d$ncis
d20$tp <- d20$refc*d20$ncis

# Generate the figures:

postscript("time-tp.eps")
plot(d$tp, d$time/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)")
dev.off()
system("rm time-tp.pdf")

postscript("time-tp-20.eps")
plot(d20$tp, d20$time/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)")
dev.off()
system("rm time-tp-20.pdf")

# Gather some statistics about the time it took to test the accepted axioms:
b <- boxplot(d$time[d$ari>1/3]/60000 ~ d$tp[d$ari>1/3], plot = FALSE)
time <- b$stats[5, ] # get the maximum time for each group
tp <- as.numeric(b$names) # get the tp value for each group

postscript("time-tp-acc.eps")
plot(d$tp[d$ari>1/3], d$time[d$ari>1/3]/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)")
# Add a dashed linear trend line and a dotted time-out line:
fit <- glm(d$time[d$ari>1/3]/60000 ~ d$tp[d$ari>1/3])
abline(fit, lty = 2) # the trend line
abline(a = 2, b = 2*fit$coefficients[2], lty = 3) # the time-out line: 2 min + 2*b*tp
fit.max <- glm(time ~ tp)
abline(fit.max, lty = 4) # the trend line, computed on the maxima for each group
abline(a = 2, b = fit.max$coefficients[2], lty = 5) # the relevant time out line: 2 min + b*tp
dev.off()
system("rm time-tp-acc.pdf")

postscript("time-tp-acc-20.eps")
plot(d20$tp[d20$ari>1/3], d20$time[d20$ari>1/3]/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)")
dev.off()
system("rm time-tp-acc-20.pdf")

postscript("time-tp-rej.eps")
plot(d$tp[d$ari<=1/3], d$time[d$ari<=1/3]/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)")
dev.off()
system("rm time-tp-rej.pdf")

postscript("time-tp-rej-20.eps")
plot(d20$tp[d20$ari<=1/3], d20$time[d20$ari<=1/3]/60000, xlab = "Time Predictor", ylab = "Elapsed Time (min)")
dev.off()
system("rm time-tp-rej-20.pdf")

postscript("time-ARI.eps")
plot(d$ari, d$time/60000, xlab = "Acceptance/Rejection Index", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
abline(v = c(1/3), col="red")
dev.off()
system("rm time-ARI.pdf")

postscript("time-ARI-20.eps")
plot(d20$ari, d20$time/60000, xlab = "Acceptance/Rejection Index", ylab = "Elapsed Time (min)", cex.lab = magnification, cex.axis = magnification)
abline(v = c(1/3), col="red")
dev.off()
system("rm time-ARI-20.pdf")

postscript("ratio-ARI.eps")
plot(d$ari, d$time/d$tp, xlab = "Acceptance/Rejection Index", ylab = "Time/Predictor Ratio", cex.lab = magnification, cex.axis = magnification)
abline(v = c(1/3), col="red")
dev.off()
system("rm ratio-ARI.pdf")

postscript("ratio-ARI-20.eps")
plot(d20$ari, d20$time/d20$tp, xlab = "Acceptance/Rejection Index", ylab = "Time/Predictor Ratio", cex.lab = magnification, cex.axis = magnification)
abline(v = c(1/3), col="red")
dev.off()
system("rm ratio-ARI-20.pdf")

postscript("ARI-BLS.eps")
plot(d$bls ~ d$ari, xlab = "Acceptance/Rejection Index", ylab = "Bühmann and Lehmann's Score", cex.lab = magnification, cex.axis = magnification)
abline(h = c(.7), col="blue")
abline(v = c(1/3), col="red")
dev.off()
system("rm ARI-BLS.pdf")

postscript("ARI-BLS-20.eps")
plot(d20$bls ~ d20$ari, xlab = "Acceptance/Rejection Index", ylab = "Bühmann and Lehmann's Score", cex.lab = magnification, cex.axis = magnification)
abline(h = c(.7), col="blue")
abline(v = c(1/3), col="red")
dev.off()
system("rm ARI-BLS-20.pdf")

# Save the data frame to a file:
write.table(d, "axiom-statistics.txt", row.names = FALSE)
write.table(d20, "axiom-statistics-20.txt", row.names = FALSE)

