#!/usr/bin/env Rscript
require("ggplot2")
require("dplyr")

data <- read.csv("generated/observations.csv")

p <- ggplot(data, aes(x=value)) +
  geom_histogram() +
  xlab("Observation") + 
  ylab("Density") + 
  theme_bw()
ggsave("data.pdf", width = 15, height = 5)

data <- read.csv("generated/permutations.csv")

p <- ggplot(data, aes(x = factor(index_0),fill = factor(value))) + 
  geom_bar(position = "fill") +
  xlab("Group index") + 
  ylab("Proportion") + 
  facet_grid(permutation_index ~ .) + 
  theme_bw()

ggsave("permutations-truth.pdf", width = 15, height = 20)
