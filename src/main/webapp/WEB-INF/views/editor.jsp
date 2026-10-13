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
</main>
<%@ include file="footer.jspf" %>
</body>
</html>
