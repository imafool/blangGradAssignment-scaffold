#!/usr/bin/env Rscript
require("ggplot2")
require("dplyr")

data <- read.csv("samples/permutations.csv")

p <- ggplot(data, aes(x = factor(index_0),fill = factor(value))) + 
  geom_bar(position = "fill") +
  xlab("Group index") + 
  ylab("Proportion") + 
  facet_grid(permutation_index ~ .) + 
  theme_bw()

ggsave("permutations-posterior.pdf", width = 15, height = 20)

data <- read.csv("samples/means.csv")

p <- ggplot(data, aes(x=sample,y=value,colour=factor(index_0))) +
  geom_line() +
  theme_bw()
ggsave("means-trace.pdf", width = 15, height = 5)
