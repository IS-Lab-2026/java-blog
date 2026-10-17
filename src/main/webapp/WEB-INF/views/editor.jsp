<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Editor studio | Papertrail</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Newsreader:opsz,wght@6..72,400;6..72,500;6..72,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/assets/css/site.css'/>">
</head>
<body>
<%@ include file="header.jspf" %>
<main class="workspace-shell">
    <div class="workspace-heading"><div><span class="eyebrow">Editor studio</span><h1>Your stories, in progress.</h1><p>Shape a draft, then publish it when it is ready.</p></div><a class="button button-outline" href="<c:url value='/posts'/>">View public journal</a></div>
    <c:if test="${param.saved eq '1'}"><p class="form-success">Story saved.</p></c:if>
    <div class="editor-layout">
        <section class="editor-form-panel">
            <div class="section-heading"><div><span class="eyebrow"><c:choose><c:when test="${not empty editPost}">Continue editing</c:when><c:otherwise>New story</c:otherwise></c:choose></span><h2><c:choose><c:when test="${not empty editPost}">Make it yours.</c:when><c:otherwise>Start with a thought.</c:otherwise></c:choose></h2></div></div>
            <form class="form-stack" method="post" action="<c:url value='/editor'/>">
                <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>"/>
                <c:if test="${not empty editPost}"><input type="hidden" name="blogId" value="<c:out value='${editPost.blogId}'/>"/></c:if>
                <label>Story title<input name="title" maxlength="500" value="<c:out value='${editPost.title}'/>" placeholder="A title readers will remember" required></label>
                <label>Short introduction<textarea name="description" maxlength="2000" rows="3" placeholder="What is this story about?" required><c:out value="${editPost.blogDescription}"/></textarea></label>
                <label>Category<select name="categoryId"><option value="">No category</option><c:forEach var="category" items="${categories}"><option value="<c:out value='${category.categoryId}'/>" <c:if test="${editPost.categoryId eq category.categoryId}">selected</c:if>><c:out value="${category.name}"/></option></c:forEach></select></label>
                <label>Story<textarea class="story-editor" name="body" maxlength="100000" rows="15" placeholder="Write your story..." required><c:out value="${editPost.body}"/></textarea></label>
                <div class="form-actions"><button class="button button-outline" name="action" value="draft" type="submit">Save draft</button><button class="button" name="action" value="publish" type="submit">Publish story <span aria-hidden="true">&rarr;</span></button></div>
            </form>
        </section>
        <aside class="editor-list-panel">
            <div class="section-heading"><div><span class="eyebrow">Your desk</span><h2>Recent stories</h2></div><span class="count-pill"><c:out value="${posts.size()}"/></span></div>
            <div class="desk-list">
                <c:forEach var="post" items="${posts}">
                    <article class="desk-item"><div><span class="status-dot <c:if test='${post.blogStatus eq "live"}'>status-live</c:if>"></span><span class="eyebrow"><c:out value="${post.blogStatus}"/></span><h3><c:out value="${post.title}"/></h3><small><c:out value="${post.views}"/> reads &middot; <c:out value="${post.likes}"/> likes</small></div><a class="text-link" href="<c:url value='/editor'><c:param name='id' value='${post.blogId}'/></c:url>">Edit</a></article>
                </c:forEach>
                <c:if test="${empty posts}"><p class="empty-note">Your first story starts here.</p></c:if>
            </div>
        </aside>
    </div>
    <section class="admin-section">
        <div class="section-heading"><div><span class="eyebrow">Audience</span><h2>Story engagement</h2></div></div>
        <div class="table-wrap"><table><thead><tr><th>Story</th><th>Status</th><th>Reads</th><th>Likes</th><th>Comments</th></tr></thead><tbody>
            <c:forEach var="item" items="${engagement}"><tr><td><c:out value="${item.title}"/></td><td><span class="status-pill"><c:out value="${item.blog_status}"/></span></td><td><c:out value="${item.views}"/></td><td><c:out value="${item.like_count}"/></td><td><c:out value="${item.comment_count}"/></td></tr></c:forEach>
            <c:if test="${empty engagement}"><tr><td colspan="5" class="table-empty">Publish a story to see its engagement.</td></tr></c:if>
        </tbody></table></div>
    </section>
    <div class="admin-overview-grid editor-audience-grid">
        <section class="admin-section">
            <div class="section-heading"><div><span class="eyebrow">Conversation</span><h2>Recent comments</h2></div></div>
            <div class="message-list"><c:forEach var="comment" items="${recentComments}"><article class="message-item"><div class="message-heading"><div><span class="eyebrow"><c:out value="${comment.user_name}"/> on <c:out value="${comment.post_title}"/></span><time><c:out value="${comment.created_at}"/></time></div></div><p><c:out value="${comment.comment}"/></p></article></c:forEach><c:if test="${empty recentComments}"><p class="empty-note">No comments on your stories yet.</p></c:if></div>
        </section>
        <section class="admin-section">
            <div class="section-heading"><div><span class="eyebrow">Community</span><h2>Subscribers</h2></div><span class="count-pill"><c:out value="${subscribers.size()}"/></span></div>
            <div class="table-wrap"><table><thead><tr><th>Reader</th><th>Following since</th></tr></thead><tbody>
                <c:forEach var="subscriber" items="${subscribers}"><tr><td><c:out value="${subscriber.user_name}"/></td><td><c:out value="${subscriber.subscription_date}"/></td></tr></c:forEach>
                <c:if test="${empty subscribers}"><tr><td colspan="2" class="table-empty">No subscribers yet.</td></tr></c:if>
            </tbody></table></div>
        </section>
    </div>
</main>
<%@ include file="footer.jspf" %>
</body>
</html>
