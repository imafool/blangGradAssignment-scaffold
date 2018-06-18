library(ggplot2)

NUM_SAMPLES = 2000
MS_TO_S = 1000

setwd("./deliverables/permuted-clustering_stpi/")
write(getwd(), stdout())

csv = read.csv("stpi_aggregated.csv")
csv$stpi = csv$stpi / NUM_SAMPLES / MS_TO_S

gg = ggplot(csv, aes(x=groupSize)) +
  geom_point(aes(y=stpi))

ggsave("stpi_plot.pdf")
