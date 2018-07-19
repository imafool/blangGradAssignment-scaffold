library(ggplot2)

args = commandArgs(trailingOnly = TRUE)
data = read.csv(args[1], header = TRUE)

data["fnName"] = paste(data$testIndex, data$targetIndex)
dat = data[,c(1,5,6)]
dat = dat[complete.cases(dat), ]
dat$groupSize = log(dat$groupSize)
dat$essps = log(dat$essps)
gg = ggplot(dat, aes(x=groupSize, y=essps, group=groupSize)) +
  geom_boxplot(outlier.shape = NA) +
  ylab("Log ESS/iter") +
  xlab("Log-Group-Size")
ggsave("essps_plot.pdf")
