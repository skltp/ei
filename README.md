# Engagemangsindex

Branch type: Prototype

Not under use for any plan to merge this branch to any other branch yet. Reason for creating this was to fix VP007 problem in VP caused by most of the aggregated services due to lack of authorization to access a given producer.

The data is fetched by FindContent and passed to the callee without checking if the callee can really access them. This branch shows a prototype where it showcases that the data can be filtered at this level. But since there is one more version of the service contract on EI to be implemented and this cannot be taken before that due to administrative problems. 

Instead the implementation to stop accessing the clients which a consumer cannot is handled currently in AgP Core component under skltp

For more details read:
https://skl-tp.atlassian.net/wiki/pages/viewpage.action?pageId=51347509#VP-007iAggregerandetjänster-Implementation2underkortsiktiglösning: