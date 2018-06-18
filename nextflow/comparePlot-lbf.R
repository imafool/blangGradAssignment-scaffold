library(ggplot2)
args = commandArgs(trailingOnly = TRUE)
perm = paste(getwd(), "/deliverables/permuted-clustering/", args[1], "/aggregated_", args[1], ".csv",sep="")
perm_lb = paste(getwd(), "/deliverables/permuted-clustering/", args[2], "/aggregated_", args[2], ".csv",sep="")

data = read.csv(perm, header = TRUE)
data_lb = read.csv(perm_lb, header = TRUE)

dat = data[,c(1,5)]
dat[complete.cases(dat), ]

dat_lb = data_lb[,c(1,5)]
dat_lb[complete.cases(dat_lb), ]

dat$lbessps = dat_lb$essps

gg = ggplot(dat, aes(x=groupSize)) +
                 geom_boxplot(aes(y=log(essps), group=groupSize, colour="red")) +
                 geom_boxplot(aes(y=log(lbessps), group=groupSize, color="blue"))

ggsave(paste(getwd(), 
            "/deliverables/permuted-clustering/essps_comparison_plot.pdf",
            sep="")
        )
