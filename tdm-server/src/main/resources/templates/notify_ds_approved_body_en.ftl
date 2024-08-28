<#include "notify_ds_header_en.ftl">
The following datasource review has been approved. See ${url}

<#if comment?has_content>Comments
${requestedBy}:
${comment}
</#if>

<#include "notify_ds_footer_en.ftl">
