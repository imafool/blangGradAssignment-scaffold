require(dplyr)
require(ggplot2)
require(gridExtra)

# IMPORTANT:
# 1. Plot is offset by OFFSET to allow log-log plot. (exists negative values)
# 2. Plotting from groupsize > exp(THRESH); looks like there are outliers

CleanData <- function(data){
  # groupSize, essps (wrong name, should be just ess), mean
  data <- data[,c(2,6,7)]
  data <- data[complete.cases(data),]
  NUM_ITER <- 1
  OFFSET <- 0.5
  MEAN <- 0.5
  HEAD <- 5
  GROUP_THRESH <- 0
  DIST_THRESH <- 0.2
  data$ess_per_iter <- data$essps/NUM_ITER
  data$log_groupSize <- log(data$groupSize)
  data$dist <- abs(data$mean - MEAN)
  data <- data %>%
    arrange_(~dist) %>%
    arrange_(~groupSize) %>%
    group_by_(~groupSize) %>%
    # do(head(.,n=HEAD)) %>%
    filter(dist < DIST_THRESH) %>%
    as.data.frame()
  print(data)
 
  # log(mean ess per iter by groupSize)
  agg_data <- aggregate(data[,4], list(data$groupSize), mean)
  # NOTE: Transformed by shifting up by OFFSET
  agg_data$x <- agg_data$x + OFFSET
  agg_data$Group.1 <- log(agg_data$Group.1)
  agg_data$COUNT <- aggregate(data[,4], list(data$groupSize), length)$x
  agg_data$sd <- aggregate(data[,4], list(data$groupSize), sd)$x
  agg_data$x_plus <- log(agg_data$x + agg_data$sd)
  agg_data$x_minus <- log(agg_data$x - agg_data$sd)
  agg_data$x <- log(agg_data$x)
  agg_data <- subset(agg_data, Group.1 > GROUP_THRESH)
  return (agg_data)
}


# @param name: default "" or "LB"
LoadData <- function(name=""){
  path_name <- paste("/home/kevin/blang/blangGradAssignment-scaffold/nextflow/deliverables/permuted-clustering/PermutationSampler",
                     name,
                     "/GS3-60LBF0.5/aggregated.csv",
                     sep="")
  data <- read.csv(path_name)
  return (data)
}


generate_plots <- function(data_naive, data_lb) {
  naive <- ggplot(data_naive, aes(x=Group.1, y=x)) +
    geom_line(outlier.shape = NA) +
    ylab("Log ESS/iter") +
    xlab("Log-Group-Size") +
    ylim(c(-5,1))+
    ggtitle("NAIVE")
  
  lb <- ggplot(data_lb, aes(x=Group.1, y=x)) +
    geom_line(outlier.shape = NA) +
    ylab("Log ESS/iter") +
    xlab("Log-Group-Size") +
    ylim(c(-5,1)) +
    ggtitle("LB")
  grid.arrange(naive, lb)
  setwd("/home/kevin/sandbox")
  ggsave("loglog_ESS_PER_ITER.pdf", arrangeGrob(naive, lb))
}

generate_plot <- function(data_naive, data_lb){
  setwd("/home/kevin/sandbox")
  plot <- ggplot() +
    xlab("log(Group Size)") +
    ylab("log(ESS/Iter)") +
    # Confidence band
    # geom_ribbon(data=data_naive, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.05, fill='blue') +
    # geom_ribbon(data=data_lb, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.35, fill='pink') +
    # Trace
    geom_line(data=data_naive, aes(x=Group.1, y=x, colour='Naive')) +
    geom_line(data=data_lb, aes(x=Group.1, y=x, colour='LB')) +
    # Regression Line
    geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x), se=FALSE) +
    geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x), color='red', se=FALSE) +
    # Regressio Line CB
    geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_plus), linetype='dotted', se=FALSE)+
    geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_minus), linetype='dotted', se=FALSE)+
    geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_plus), color='red', linetype='dotted', se=FALSE)+
    geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_minus),color='red', linetype='dotted', se=FALSE)
  
  ggsave('ESSperIterVSGroupSize.pdf')
  plot
}

data_naive <- CleanData(LoadData(""))
data_lb <- CleanData(LoadData("LB"))
generate_plot(data_naive, data_lb)

