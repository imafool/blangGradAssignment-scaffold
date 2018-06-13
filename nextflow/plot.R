library(ggplot2)
args = commandArgs(trailingOnly = TRUE)
data = read.csv(args[1], header = TRUE)

data["fnName"] = paste(data$testIndex, data$targetIndex)
dat = data[,c(1,5,6)]
dat[complete.cases(dat), ]
gg = ggplot(dat, aes(x=groupSize, y=essps, group=groupSize)) + geom_boxplot()
ggsave("essps_plot.pdf")
