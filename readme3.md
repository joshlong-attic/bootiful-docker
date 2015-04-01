# Lattice, a Distributed Runtime for your Containerized Spring Workloads

## Portable Applications

Spring's always made building portable applications as simple as possible. The very idea of dependency injection  provides the crucial bit of indirection required to insulate an application from an underlying platform, and there are numerous patterns that promote cloud-ready, portable, and movable applications. The [12 Factor App](http:/12factor.net) manifesto prescribes many good recommendations. We've even covered some of them, like  [backing services](https://spring.io/blog/2015/01/27/12-factor-app-style-backing-services-with-spring-and-cloud-foundry) and [externalized configuration](https://spring.io/blog/2015/01/13/configuring-it-all-out-or-12-factor-app-style-configuration-with-spring), in this very space recently.

As developers, we're used to being able to test applications in isolation, to validate the inputs into an application and validate the resulting behavior.  We're used to reproducible builds; we're used to being able to throw away the build and - with the disciplined use of tags and so on - reproduce the same build. This isn't news.

## Cattle

It took us developers a while to get to this place, but we did. In the last 8 years we've seen this rigor come to operations teams. Operations want reproducible infrastructure as much as we developers want reproducible builds. Consistency and reproducibility are even more important with disposable, ephemeral, cloud infrastructure. As Adrian Cochraft (formerly at Netflix) reminds us: treat servers like cattle, not pets.

It's 2015, things have come a long way. Developers and operations are - ideally at least - two highly integrated teams. Developers want consistency in the way their applications run, operations want consistency in their infrastructure.  

## Deploy Containers

[Linux Containers, or _LXC_](http://wikipedia.com/wiki/LXC), are a set of features in the Linux kernel designed to isolate applications. Applications run as though they've got the run of the kernel. LXC aren't usable out of the box. Container technologies like Docker and Rocket provide a very simple wya to declaratively leverage LXC features. Using Docker, for example, you can write a Dockerfile, check it into your code and then use that Docker file and code to reproduce both the application and its environment, and do so at a much lower runtime footprint than traditional virtualization. This makes things simpler: operations deploy containers, not applications.

The only thing that remains, then, is automation around managing, running and scaling containerized workloads. This is where a distributed runtime like [Lattice](http://lattice.cf) comes in. From the site:

> Lattice aspires to make clustering containers easy. Lattice includes a cluster scheduler, http load balancing, log aggregation and health management. Lattice containers can be long running or temporary tasks which get dynamically scaled and balanced across a cluster. Lattice packages components from Cloud Foundry to provide a cloud native platform for individual developers and small teams.

## A Simple Spring Boot Application

Let's get a simple example working.
