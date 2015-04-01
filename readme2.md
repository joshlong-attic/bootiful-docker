# The Lattice Distributed Runtime

- the value of consistency
 * Netflix' deploy containers
 * consistency in operations means ease of moving to production
 * Spring's always brought you app portability; ease of environment adapation through the use of dpeendency injection. If youve been following some of the recent blogs, you see how SPring also supports patterns designed to promote apps that can be deployed on, and managed by, a distributed runtime like CF. (12f config and backing services posts, as well as the one on deploying Spring Boot apps, for example)
 * this is the developer side of the story. Apps thar can be moved from one environment to another (dev, qa prod, etc) with ease.
 * theres also the operations perspective: how hard is it to get those environments setup in the first place? having a consistent environment, removing snowflakes, reduces friction in movjing to production. weve seeb this time and time again with cloud foundry, what we;ve called a PaaS. It lets developers declaratively describe what their applications need at runtime, andit it leaves operations free to worry about more pressing matters. they no longer need to get involved in every development team's push to production. and why would they? the developers know what their apps need, anyway.
 * netflix, for example, has always said that they deploy containers, not applications.
 * these days, the most popular way to describe an application's runtime is to use some variantion of linux containers, and - specificlaly - Docker.  
 - LXc is not a single API so much as a set of kernel features designed to isolate an application and its resources from the rest of the system. the application thinks it has the run of the system but in reality its sharing a host machine with any number of other containers.
 - unlike VMs, LXC containers share a base kernel, and so don't need to


 Lattice is an open source project for running containerized workloads on a cluster. A Lattice cluster is comprised of a number of Lattice Cells (VMs that run containers) and a Lattice Coordinator that monitors the Cells.

Lattice includes built-in http load-balancing, a cluster scheduler, log aggregation with log streaming and health management.

Lattice containers are described as long-running processes or temporary tasks. Lattice includes support for Linux Containers expressed either as Docker Images or by composing applications as binary code on top of a root file system. Lattice's container pluggability will enable other backends such as Windows or Rocket in the future.

All this functionality leverages components of Cloud Foundry. The new parts in Lattice are the CLI to interact with the component APIs and the installers.
