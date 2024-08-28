<#include "notify_ds_header_en.ftl">
The request to review the following datasource has been cancelled. See ${url}

<#if comment?has_content>Comments
${requestedBy}:
${comment}
</#if>

<#include "notify_ds_footer_en.ftl">
