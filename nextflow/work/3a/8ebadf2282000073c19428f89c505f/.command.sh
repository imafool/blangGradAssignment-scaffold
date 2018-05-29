#!/usr/bin/env Rscript
wd <- getwd()
outputDir <- dirname(dirname(dirname(getwd())))
trace <- read.table(paste(wd,'/../../../trace.txt', sep = ""), sep = '	', header = TRUE)
options(digits = 4)
timeToRunInf <- as.double(substr(trace[5,8], 0, 4))
#timeToRunInf <- file(paste(wd,"/ESS.txt",sep = ""))
write(timeToRunInf, file=paste(outputDir,"/ESS.txt", sep = ""))
