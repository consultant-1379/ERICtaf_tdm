<#include "notify_ds_header_en.ftl">
Please visit ${url} to review the datasource.

<#if comment?has_content>Comments
${requestedBy}:
${comment}
</#if>

<#include "notify_ds_footer_en.ftl">
