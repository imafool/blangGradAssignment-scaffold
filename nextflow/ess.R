  args = commandArgs(trailingOnly = TRUE)
  nGroups = as.integer(args[1])
  groupSize = as.integer(args[2])
#  nGroups = $nGroups
#  groupSize = $groupSize
  from_vertex = 0
  to_vertex = 0
  
  trace = read.table("../../../trace.txt",sep='\t',header=TRUE)
  data <- read.csv("generated_${x}/permutations.csv")

  dur = as.double(substr(trace[5,8],0,4))

  x = rep(0,as.integer(dim(data)[1]/(groupSize*nGroups)))
  k = 0
  for (i in 1:as.integer(dim(data)[1]/(groupSize*nGroups))) {
    for (j in 1:groupSize) {
      if (j == from_vertex+1 & as.integer(data[k+j,'value']) == to_vertex) {
        x[i] = 1
      }
    }
    k = k + groupSize*nGroups
  }

  N = length(x)
  v_up = sum((x-sum(x)/N)^2)/(N-1)

  I = x[1:sqrt(N)]
  batch_size = sqrt(N)

  incr = floor(sqrt(N))
  up_idx = floor(sqrt(N))
  num_batch = N%/%incr
  I = rep(0,num_batch)

  i = 1
  while (i<=num_batch) {
    x_batch = x[(up_idx-incr+1):up_idx]
    I[i] = mean(x_batch)
    up_idx = up_idx + incr
    i = i + 1
  }
  
  M = length(I)
  v_down = sum((I-sum(I)/M)^2)/(M-1)
  ess = v_up/v_down*sqrt(N)
  ess_per_sec = ess/dur

  write(paste("ess_per_sec =",ess/dur),file="../../../deliverables/permuted-clustering/ess_per_sec.txt")


