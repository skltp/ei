This project is used to test raw ActiveMQ performance using the ActiveMQ
performance module.

Ref:
http://activemq.apache.org/activemq-performance-module-users-manual.html
============================================================

Bakground
----------
The purpose of testing raw ActiveMQ performance is to understand if there
are any ActiveMQ related application performance bottlenecks when running
application performance tests.


Note: Mule ESB (v3.3.1) uses a single ActiveMQ connection which is a
bootleneck in achiving really high performance for ActiveMQ since high
throughput requires multiple conenctions to the broker.

Ref: https://skl-tp.atlassian.net/browse/SKLTP-667


Usage
------
There are two scripts (message producer and consumer respectively) that
demonstrates the usage:

1. run-prod1-test.sh
2. run-cons1-test.sh

each script refers to a property file for the test.
