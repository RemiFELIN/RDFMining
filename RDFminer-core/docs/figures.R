poss <- function(tot, confirmations, counterexamples)
{
	1 - sqrt(1 - ((tot - counterexamples)/tot)^2)
}
nec <- function(tot, confirmations, counterexamples)
{
	ifelse(counterexamples == 0,
		sqrt(1 - ((tot - confirmations)/tot)^2),
		0)
}
dposs <- function(tot, confirmations, counterexamples)
{
	ifelse(confirmations == 0,
		1 - sqrt(1 - ((tot - counterexamples)/tot)^2),
		1)
}
dnec <- function(tot, confirmations, counterexamples)
{
	sqrt(1 - ((tot - confirmations)/tot)^2)
}

tnorm <- function(x, y)
{
	nx <- sqrt(1 - x*x)
	ny <- sqrt(1 - y*y)
	arg <- 2*(nx + ny - nx*ny - y*y*nx - x*x*ny + x*x + y*y - 1) - x*x*y*y
	z <- sqrt(ifelse(arg>=0, arg, 0))
}

f <- function(n)
{
	poss(100, 0, n)
}
x <- seq(0, 100)
y <- sapply(x, f)
pdf("possibility.pdf")
plot(x, y, type = "l", xlab = "no. of counterexamples", ylab = "possibility")
dev.off()

f <- function(n)
{
	nec(100, n, 0)
}
x <- seq(0, 100)
y <- sapply(x, f)
pdf("necessity.pdf")
plot(x, y, type = "l", xlab = "no. of confirmations", ylab = "necessity")
dev.off()

x <- seq(0, 100, 2)
y <- x
f <- function(x, y)
{
	ifelse(x + y <= 100,
		nec(100, x, y) + poss(100, x, y) - 1,
		NA)
}
z <- outer(x, y, f)
pdf("ARI-c.pdf")
persp(x, y, z, phi = 45, theta = 120,
  xlab = "no. of confirmations", ylab = "no. of counterexamples",
  zlab = "ARI = possibility + necessity - 1", ticktype = "detailed") -> res
lines(trans3d(c(0, 100), c(100, 0), c(-1, -1), pmat = res), col = "black")
dev.off()

x <- seq(0, 100, 2)
y <- x
f <- function(x, y)
{
	ifelse(x + y <= 100,
		dnec(100, x, y) + dposs(100, x, y) - 1,
		NA)
}
z <- outer(x, y, f)
pdf("ARI-d.pdf")
persp(x, y, z, phi = 45, theta = 120,
  xlab = "no. of confirmations", ylab = "no. of counterexamples",
  zlab = "ARI = possibility + necessity - 1", ticktype = "detailed") -> res
lines(trans3d(c(0, 100), c(100, 0), c(-1, -1), pmat = res), col = "black")
dev.off()

x <- seq(0, 1, 0.02)
y <- x
z <- outer(x, y, tnorm)
pdf("t-norm.pdf")
persp(x, y, z, phi = 45, theta = 30,
  xlab = "x", ylab = "y",
  zlab = "T(x, y)", ticktype = "detailed")
dev.off()

x <- seq(0, 1, 0.02)
y <- x
f <- function(x, y)
{
	ifelse(x + y == 0, 0, x*y/(x + y - x*y))
}
z <- outer(x, y, f)
pdf("Hamacher.pdf")
persp(x, y, z, phi = 45, theta = 30,
  xlab = "x", ylab = "y",
  zlab = "T(x, y)", ticktype = "detailed")
dev.off()

x <- seq(0, 1, 0.02)
y <- x
f <- function(x, y)
{
	x*y
}
z <- outer(x, y, f)
pdf("product.pdf")
persp(x, y, z, phi = 45, theta = 30,
  xlab = "x", ylab = "y",
  zlab = "T(x, y)", ticktype = "detailed")
dev.off()

