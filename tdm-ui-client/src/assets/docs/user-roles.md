## User Roles

In the Test Data Management System there are 2 user roles.

 - Test Engineer
 - Test Manager


By default, each user will have view access to all the data sources.

User and their roles are applied to a context. A user may have different levels of access for different contexts.

Context structure is a tree structure that has parent and child contexts. 
User access and roles are given based on that tree structure. 
If the user has access given on the parent context then all other child contexts will be given this level of access for that user. 
This can be overwritten if new access roles are defined for a child context.

### Test Engineer
The test engineer is the lowest level of access. This role allows the user to create, copy, edit and delete from with in the context
the user was assigned.

### Test Manager
The test manager is the highest level of access. This role allows the same privileges as a test engineer. 
It also allows the user to approve a data source that has been sent for review from with in the context
where the user have privileges.


