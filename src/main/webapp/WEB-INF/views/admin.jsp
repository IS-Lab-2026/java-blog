<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Admin desk | Papertrail</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Newsreader:opsz,wght@6..72,400;6..72,500;6..72,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/assets/css/site.css'/>">
</head>
<body>
<%@ include file="header.jspf" %>
<main class="workspace-shell">
    <div class="workspace-heading"><div><span class="eyebrow">Admin desk</span><h1>Keep the journal healthy.</h1><p>Review stories, people, and the latest activity.</p></div><a class="button button-outline" href="<c:url value='/notifications'/>">Open inbox</a></div>
    <c:if test="${param.updated eq '1'}"><p class="form-success">Your changes were saved.</p></c:if>
    <nav class="admin-tabs" aria-label="Admin sections">
        <a class="<c:if test='${tab eq "overview"}'>selected</c:if>" href="<c:url value='/admin'><c:param name='tab' value='overview'/></c:url>">Overview</a>
        <a class="<c:if test='${tab eq "posts"}'>selected</c:if>" href="<c:url value='/admin'><c:param name='tab' value='posts'/></c:url>">Stories</a>
        <a class="<c:if test='${tab eq "comments"}'>selected</c:if>" href="<c:url value='/admin'><c:param name='tab' value='comments'/></c:url>">Comments</a>
        <a class="<c:if test='${tab eq "users"}'>selected</c:if>" href="<c:url value='/admin'><c:param name='tab' value='users'/></c:url>">People</a>
        <a class="<c:if test='${tab eq "logins"}'>selected</c:if>" href="<c:url value='/admin'><c:param name='tab' value='logins'/></c:url>">Logins</a>
        <a class="<c:if test='${tab eq "messages"}'>selected</c:if>" href="<c:url value='/admin'><c:param name='tab' value='messages'/></c:url>">Contact</a>
    </nav>
    <c:if test="${tab eq 'overview'}">
        <section class="metric-grid">
            <article class="metric"><span class="eyebrow">Accounts</span><strong><c:out value="${metrics.users}"/></strong><span>registered readers and editors</span></article>
            <article class="metric"><span class="eyebrow">Stories</span><strong><c:out value="${metrics.posts}"/></strong><span>draft and published</span></article>
            <article class="metric"><span class="eyebrow">Comments</span><strong><c:out value="${metrics.comments}"/></strong><span>reader notes across stories</span></article>
            <article class="metric metric-accent"><span class="eyebrow">Today</span><strong><c:out value="${metrics.logins}"/></strong><span>successful sign-ins</span></article>
        </section>
        <section class="admin-overview-grid"><div class="admin-note"><span class="eyebrow">Editorial pulse</span><h2>Make room for good work.</h2><p>Keep the reading experience useful: surface strong stories, support the people writing them, and step in when something needs attention.</p><a class="text-link" href="<c:url value='/admin'><c:param name='tab' value='posts'/></c:url>">Review stories &rarr;</a></div><div class="admin-quick-links"><span class="eyebrow">Workspace</span><a href="<c:url value='/admin'><c:param name='tab' value='users'/></c:url>">Manage people <span>&rarr;</span></a><a href="<c:url value='/admin'><c:param name='tab' value='comments'/></c:url>">Moderate comments <span>&rarr;</span></a><a href="<c:url value='/admin'><c:param name='tab' value='logins'/></c:url>">Review sign-ins <span>&rarr;</span></a><a href="<c:url value='/admin'><c:param name='tab' value='messages'/></c:url>">Read contact messages <span>&rarr;</span></a></div></section>
    </c:if>
    <c:if test="${tab eq 'posts'}">
        <section class="admin-section"><div class="section-heading"><div><span class="eyebrow">Publication desk</span><h2>All stories</h2></div></div>
            <div class="table-wrap"><table><thead><tr><th>Story</th><th>Editor</th><th>Status</th><th>Reads</th><th>Visibility</th><th>Action</th></tr></thead><tbody>
                <c:forEach var="post" items="${posts}"><tr><td><a class="table-title" href="<c:url value='/post'><c:param name='slug' value='${post.slug}'/></c:url>"><c:out value="${post.title}"/></a></td><td><c:out value="${post.author_name}"/></td><td><span class="status-pill"><c:out value="${post.blog_status}"/></span></td><td><c:out value="${post.views}"/></td><td><c:choose><c:when test="${post.is_visible}">Visible</c:when><c:otherwise>Hidden</c:otherwise></c:choose></td><td><form method="post" action="<c:url value='/admin'/>"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"/><input type="hidden" name="id" value="<c:out value='${post.blog_id}'/>"/><input type="hidden" name="tab" value="posts"/><button class="text-button" name="action" value="${post.is_visible ? 'hide-post' : 'show-post'}" type="submit"><c:choose><c:when test="${post.is_visible}">Hide</c:when><c:otherwise>Restore</c:otherwise></c:choose></button></form></td></tr></c:forEach>
                <c:if test="${empty posts}"><tr><td colspan="6" class="table-empty">No stories yet.</td></tr></c:if>
            </tbody></table></div>
        </section>
    </c:if>
    <c:if test="${tab eq 'comments'}">
        <section class="admin-section"><div class="section-heading"><div><span class="eyebrow">Community</span><h2>Comment moderation</h2></div></div>
            <div class="table-wrap"><table><thead><tr><th>Comment</th><th>Reader</th><th>Story</th><th>Visibility</th><th>Action</th></tr></thead><tbody>
                <c:forEach var="comment" items="${comments}"><tr><td class="comment-cell"><c:out value="${comment.comment}"/></td><td><c:out value="${comment.user_name}"/></td><td><c:out value="${comment.title}"/></td><td><c:choose><c:when test="${comment.is_visible}">Visible</c:when><c:otherwise>Hidden</c:otherwise></c:choose></td><td><form method="post" action="<c:url value='/admin'/>"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"/><input type="hidden" name="id" value="<c:out value='${comment.comment_id}'/>"/><input type="hidden" name="tab" value="comments"/><button class="text-button" name="action" value="${comment.is_visible ? 'hide-comment' : 'show-comment'}" type="submit"><c:choose><c:when test="${comment.is_visible}">Hide</c:when><c:otherwise>Restore</c:otherwise></c:choose></button></form></td></tr></c:forEach>
                <c:if test="${empty comments}"><tr><td colspan="5" class="table-empty">No comments to review.</td></tr></c:if>
            </tbody></table></div>
        </section>
    </c:if>
    <c:if test="${tab eq 'users'}">
        <section class="admin-section"><div class="section-heading"><div><span class="eyebrow">Community</span><h2>People and access</h2></div></div>
            <div class="table-wrap"><table><thead><tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Access</th><th>Action</th></tr></thead><tbody>
                <c:forEach var="person" items="${users}"><tr><td><strong><c:out value="${person.user_name}"/></strong></td><td><c:out value="${person.email}"/></td><td><span class="status-pill"><c:out value="${person.user_role}"/></span></td><td><c:out value="${person.join_date}"/></td><td><c:choose><c:when test="${person.is_active}">Active</c:when><c:otherwise>Suspended</c:otherwise></c:choose></td><td><c:if test="${person.user_role ne 'admin' and person.user_id ne sessionScope.userId}"><div class="table-actions"><form method="post" action="<c:url value='/admin'/>"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"/><input type="hidden" name="id" value="<c:out value='${person.user_id}'/>"/><input type="hidden" name="tab" value="users"/><c:choose><c:when test="${person.user_role eq 'editor'}"><button class="text-button" name="action" value="make-user">Make reader</button></c:when><c:otherwise><button class="text-button" name="action" value="make-editor">Make editor</button></c:otherwise></c:choose></form><form method="post" action="<c:url value='/admin'/>"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"/><input type="hidden" name="id" value="<c:out value='${person.user_id}'/>"/><input type="hidden" name="tab" value="users"/><c:choose><c:when test="${person.is_active}"><button class="text-button text-danger" name="action" value="suspend-user">Suspend</button></c:when><c:otherwise><button class="text-button" name="action" value="activate-user">Activate</button></c:otherwise></c:choose></form></div></c:if></td></tr></c:forEach>
                <c:if test="${empty users}"><tr><td colspan="6" class="table-empty">No accounts yet.</td></tr></c:if>
            </tbody></table></div>
        </section>
    </c:if>
    <c:if test="${tab eq 'logins'}">
        <section class="admin-section"><div class="section-heading"><div><span class="eyebrow">Security</span><h2>Recent sign-ins</h2></div></div>
            <div class="table-wrap"><table><thead><tr><th>Account</th><th>Email</th><th>Time</th><th>Address</th></tr></thead><tbody><c:forEach var="login" items="${logins}"><tr><td><c:out value="${login.user_name}"/></td><td><c:out value="${login.email}"/></td><td><c:out value="${login.login_time}"/></td><td><c:out value="${login.ip_address}"/></td></tr></c:forEach><c:if test="${empty logins}"><tr><td colspan="4" class="table-empty">No sign-ins recorded.</td></tr></c:if></tbody></table></div>
        </section>
    </c:if>
    <c:if test="${tab eq 'messages'}">
        <section class="admin-section"><div class="section-heading"><div><span class="eyebrow">Reader mail</span><h2>Contact messages</h2></div></div>
            <div class="message-list"><c:forEach var="message" items="${messages}"><article class="message-item"><div class="message-heading"><div><span class="eyebrow"><c:out value="${message.sender_name}"/> &middot; <c:out value="${message.sender_email}"/></span><h3><c:out value="${message.subject}"/></h3></div><c:choose><c:when test="${message.is_resolved}"><span class="status-pill">Resolved</span></c:when><c:otherwise><form method="post" action="<c:url value='/admin'/>"><input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"/><input type="hidden" name="id" value="<c:out value='${message.message_id}'/>"/><input type="hidden" name="tab" value="messages"/><button class="text-button" name="action" value="resolve-message">Mark resolved</button></form></c:otherwise></c:choose></div><p><c:out value="${message.message}"/></p><time><c:out value="${message.submitted_at}"/></time></article></c:forEach><c:if test="${empty messages}"><p class="empty-note">No messages in the inbox.</p></c:if></div>
        </section>
    </c:if>
</main>
<%@ include file="footer.jspf" %>
</body>
</html>
