<#include "notify_ds_header_en.ftl">
The datasource for review has been rejected. See ${url}

<#if comment?has_content>Comments
${requestedBy}:
${comment}
</#if>

<#include "notify_ds_footer_en.ftl">
