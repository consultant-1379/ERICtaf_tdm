## Approve Data Source

- Upon creation of a Data Source, it will be in ![](assets/images/other/unapproved_label.png) state by default
- Only ![](assets/images/other/approved_label.png) data sources can be used in TAF tests. In order to do so, Data Source must be reviewed by an appropriate 
  <a ng-click="vm.scrollTo('reviewers')">reviewer</a>

<a name="workflow"></a>
### Approval Process Workflow

- Approval Process Workflow looks the following way:   
  <i class="fa fa-play-circle"></i> <i class="fa fa-long-arrow-right"></i>
  ![](assets/images/other/unapproved_label.png) <i class="fa fa-arrows-h"></i>
  ![](assets/images/other/pending_label.png) <i class="fa fa-long-arrow-right"></i> 
  ![](assets/images/other/rejected_label.png) **OR** ![](assets/images/other/approved_label.png)
  <i class="fa fa-long-arrow-right"></i> <i class="fa fa-stop-circle"></i> 
  - Data Source **Author** can <a ng-click="vm.scrollTo('send')">Send Request</a>:
    ![](assets/images/other/unapproved_label.png) <i class="fa fa-long-arrow-right"></i> ![](assets/images/other/pending_label.png)  
  - Data Source **Author** can <a ng-click="vm.scrollTo('cancel')">Cancel Request</a>:
    ![](assets/images/other/pending_label.png) <i class="fa fa-long-arrow-right"></i> ![](assets/images/other/unapproved_label.png)
  - Data Source **Reviewer** can <a ng-click="vm.scrollTo('approve')">Approve Request</a>:
    ![](assets/images/other/pending_label.png) <i class="fa fa-long-arrow-right"></i> ![](assets/images/other/approved_label.png)
  - Data Source **Reviewer** can <a ng-click="vm.scrollTo('reject')">Reject Request</a>:
    ![](assets/images/other/pending_label.png) <i class="fa fa-long-arrow-right"></i> ![](assets/images/other/rejected_label.png)
- Data Source cannot be edited while its Approval Status is ![](assets/images/other/pending_label.png) 
- ![](assets/images/other/approved_label.png) and ![](assets/images/other/rejected_label.png) are terminal statuses and cannot be reverted
- Approval Status transitions **do not** increase Data Source Version 
- <a ui-sref="documentation.datasource-create({'#': 'version'})">Version</a> increment **will reset** Approval Status to ![](assets/images/other/unapproved_label.png) 

<a name="reviewers"></a>
### Reviewers

- Users with Test Manager role can only review the data sources

<a name="send"></a>
### Send Request

1. Click on ![](assets/images/buttons/request_approval.png) to open **Request Approval** popup:
  <img src="assets/images/popups/request_approval.png" class="docs-screenshot"></img>
1. Enter reviewer's email or signum:
  <img src="assets/images/widgets/reviewers.png" class="docs-screenshot"></img>
    You can Click <i class="fa fa-arrow-down"></i> key to see the first ten reviewers
1. If user entered is not found you see the following:
<img src="assets/images/popups/user_not_found.png" class="docs-screenshot"></img>
1. Click on ![](assets/images/buttons/send_request.png) to confirm your selection  
Specified reviewers will receive a corresponding email and the Data Source will become ![](assets/images/other/pending_label.png) and read-only

<a name="cancel"></a>
### Cancel Request

1. Click ![](assets/images/buttons/cancel_request.png) to revert Approval Request
1. The Data Source will become ![](assets/images/other/unapproved_label.png) again

### Review Changes

- Click ![](assets/images/buttons/review.png) to open the review page:
  <img src="assets/images/screens/review_page.png" class="docs-screenshot"></img>
- Newly edited data cells will be displayed in yellow.
- When you hover over a changed cell the previous value will be shown in the tool tip and if the cell is new, the text "No previous value" will appear.
- Deleted data records will be displayed in red.

<a name="approve"></a>
### Approve Request

1. Click ![](assets/images/buttons/approve.png) to open **Approval Comment** popup:
  <img src="assets/images/popups/approval_comment.png" class="docs-screenshot"></img>
1. An optional comment can be mentioned while approving
1. Click ![](assets/images/buttons/approve.png) again to confirm your decision
1. The Data Source will become ![](assets/images/other/approved_label.png) 
  and usable in TAF

<a name="reject"></a>
### Reject Request

1. Click ![](assets/images/buttons/reject.png) to open **Reject Comment** popup:
  <img src="assets/images/popups/reject_comment.png" class="docs-screenshot"></img>
1. **Mandatory** comment should be provided for rejecting
1. Click ![](assets/images/buttons/reject.png) again to confirm your decision
1. The Data Source will become ![](assets/images/other/rejected_label.png)

<a name="unApprove"></a>
### UnApprove Data Source

1. Click ![](assets/images/buttons/un-approve-red.png) to open **Un-approval Comment** popup:
  <img src="assets/images/popups/un_approval_comment.png" class="docs-screenshot"></img>
1. **Mandatory** comment should be provided for un-approving
1. Press ![](assets/images/buttons/un-approve.png) again to confirm your decision
1. The Data Source will become ![](assets/images/other/unapproved_label.png)  
<i class="fa fa-info-circle text-info"></i> **Note**: Only the latest approved datasource can be unapproved

---

Read next: <a ui-sref="documentation.datasource-label">Add Label to Data Source</a>
