require(dplyr)
require(ggplot2)
require(gridExtra)

# IMPORTANT:
# 1. Plot is offset by OFFSET to allow log-log plot. (exists negative values)

CleanData <- function(data, head=5, group_thresh=0, dist_thresh=0.2, is_lb=FALSE){
  # groupSize, essps (wrong name, should be just ess), mean
  data <- data[,c(2,6,7)]
  data <- data[complete.cases(data),]
  NUM_ITER <- 1
  OFFSET <- 0.5
  MEAN <- 0.5
  data$ess_per_iter <- data$esspi/NUM_ITER
  data <- data[complete.cases(data),]
  data$log_groupSize <- log(data$groupSize)
  data$dist <- abs(data$mean - MEAN)
  data <- data %>%
    arrange_(~dist) %>%
    arrange_(~groupSize) %>%
    group_by_(~groupSize) %>%
    filter(dist < dist_thresh) %>%
    do(head(.,n=head)) %>%
    as.data.frame()
  # log(mean ess per iter by groupSize)
  agg_data <- aggregate(data[,4], list(data$groupSize), mean)
  if (is_lb){
    agg_data$ex_per_ess <- agg_data$Group.1/agg_data$x
  } else {
    agg_data$ex_per_ess <- 1/agg_data$x
  }
  # NOTE: Transformed by shifting up by OFFSET
  agg_data$x <- agg_data$x + OFFSET
  agg_data$ex_per_ess <- log(agg_data$ex_per_ess)
  agg_data$Group.1 <- log(agg_data$Group.1)
  agg_data$COUNT <- aggregate(data[,4], list(data$groupSize), length)$x
  agg_data$sd <- aggregate(data[,4], list(data$groupSize), sd)$x
  agg_data$x_plus <- log(agg_data$x + 2*agg_data$sd)
  agg_data$x_minus <- log(agg_data$x - 2*agg_data$sd)
  agg_data$x <- log(agg_data$x)
  agg_data <- subset(agg_data, Group.1 > group_thresh)
  return (agg_data)
}

# @param name: default "" or "LB"
LoadData <- function(name=""){
  path_name <- paste("/home/kevinchern/blang/blangGradAssignment-scaffold/nextflow/deliverables/permuted-clustering/PermutationSampler",
                     name,
                     "/GS3-60LBF0.5/aggregated.csv",
                     sep="")
  data <- read.csv(path_name)
  return (data)
}

generate_plot_points <- function(data_naive, data_lb){
  setwd("/home/kevinchern/sandbox")
  plot <- ggplot() +
    xlab("log(Group Size)") +
    ylab("log(ESS/Iter)") +
    # Confidence band
    geom_ribbon(data=data_naive, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.05, fill='blue') +
    geom_ribbon(data=data_lb, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.35, fill='pink') +
    # points
    geom_point(data=data_naive, aes(x=Group.1, y=x, colour='Naive')) +
    geom_point(data=data_lb, aes(x=Group.1, y=x, colour='LB'))+
    # Regression Line
    geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x), se=TRUE)+
    geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x), color='red', se=TRUE)+
    # print(lm(x~Group.1, data_naive))
    # print(lm(x~Group.1, data_lb))
    # Regressio Line CB
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_plus), linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_minus), linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_plus), color='red', linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_minus),color='red', linetype='dotted', se=FALSE)
    labs(color="Proposals")
    
  ggsave('ESSperIterVSGroupSize.pdf')
  plot
}

generate_plot <- function(data_naive, data_lb){
  setwd("/home/kevinchern/sandbox")
  plot <- ggplot() +
    xlab("log(Group Size)") +
    ylab("log(ESS/Iter)") +
    # Confidence band
    geom_ribbon(data=data_naive, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.05, fill='blue') +
    geom_ribbon(data=data_lb, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.35, fill='pink') +
    # Trace
    geom_line(data=data_naive, aes(x=Group.1, y=x, colour='Naive')) +
    geom_line(data=data_lb, aes(x=Group.1, y=x, colour='LB')) +
    # Regression Line
    geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x), se=FALSE) +
    geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x), color='red', se=FALSE) +
    
    # Regressio Line CB
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_plus), linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_minus), linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_plus), color='red', linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_minus),color='red', linetype='dotted', se=FALSE)
  
  ggsave('ESSperIterVSGroupSize.pdf')
  plot
}

exe_per_ess_plot <- function(data_naive, data_lb){
  setwd("/home/kevinchern/sandbox")
  plot <- ggplot() +
    xlab("log(Group Size)") +
    ylab("log(Cost/ESS)") +
    # Confidence band
    # geom_ribbon(data=data_naive, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.05, fill='blue') +
    # geom_ribbon(data=data_lb, aes(x=Group.1, ymax=x_plus, ymin=x_minus), alpha=0.35, fill='pink') +
    # Trace
    geom_line(data=data_naive, aes(x=Group.1, y=ex_per_ess, colour='Naive')) +
    geom_line(data=data_lb, aes(x=Group.1, y=ex_per_ess, colour='LB')) +
    # Regression Line
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x), se=FALSE) +
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x), color='red', se=FALSE) +
    # Regressio Line CB
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_plus), linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_naive, method='lm', aes(x=Group.1, y=x_minus), linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_plus), color='red', linetype='dotted', se=FALSE)+
    # geom_smooth(data=data_lb, method='lm', aes(x=Group.1, y=x_minus),color='red', linetype='dotted', se=FALSE)
    labs(color="Proposals")
  
  ggsave('cost-per-ess.pdf')
  plot
}

main <- function(h, gt, dt){
  data_naive <- CleanData(LoadData(""), head=h, group_thresh=gt, dist_thresh=dt, is_lb=FALSE)
  data_lb <- CleanData(LoadData("LB"), head=h, group_thres=gt, dist_thresh=dt, is_lb=TRUE)
  exe_per_ess_plot(data_naive, data_lb)
  # generate_plot_points(data_naive, data_lb)
}
 
main(h=1000, gt=0, dt=1) 

