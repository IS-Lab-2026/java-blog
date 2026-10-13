<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:choose><c:when test="${register}">Create account</c:when><c:otherwise>Sign in</c:otherwise></c:choose> | Papertrail</title>
    <link rel="preconnect" href="https://fonts.googleapis.com"><link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
    <link href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Newsreader:opsz,wght@6..72,400;6..72,500;6..72,600&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="<c:url value='/assets/css/site.css'/>">
</head>
<body>
<%@ include file="header.jspf" %>
<main class="auth-layout">
    <section class="auth-art" aria-label="A quiet reading room">
        <img src="https://images.unsplash.com/photo-1507842217343-583bb7270b66?auto=format&fit=crop&w=1400&q=85" alt="Warm light across a quiet library">
        <div class="auth-art-copy"><span class="eyebrow eyebrow-light">A place for good stories</span><h1>Read with curiosity.<br>Write with heart.</h1></div>
    </section>
    <section class="auth-panel">
        <p class="eyebrow"><c:choose><c:when test="${register}">Start here</c:when><c:otherwise>Welcome back</c:otherwise></c:choose></p>
        <h2><c:choose><c:when test="${register}">Make yourself at home.</c:when><c:otherwise>Pick up where you left off.</c:otherwise></c:choose></h2>
        <c:if test="${not empty error}"><p class="form-error" role="alert"><c:out value="${error}"/></p></c:if>
        <form class="form-stack" method="post" action="<c:url value='${register ? "/register" : "/login"}'/>">
            <input type="hidden" name="csrf" value="<c:out value='${sessionScope.csrfToken}'/>" />
            <c:if test="${register}"><label>Your name<input name="name" autocomplete="name" maxlength="200" required></label></c:if>
            <label>Email address<input name="email" type="email" autocomplete="email" maxlength="250" required></label>
            <label>Password<input name="password" type="password" autocomplete="${register ? 'new-password' : 'current-password'}" minlength="8" maxlength="128" required></label>
            <button class="button button-wide" type="submit"><c:choose><c:when test="${register}">Create account</c:when><c:otherwise>Sign in</c:otherwise></c:choose><span aria-hidden="true">&rarr;</span></button>
        </form>
        <p class="form-switch"><c:choose><c:when test="${register}">Already have an account? <a href="<c:url value='/login'/>">Sign in</a></c:when><c:otherwise>New to Papertrail? <a href="<c:url value='/register'/>">Create an account</a></c:otherwise></c:choose></p>
        <p class="form-note">New accounts start as readers. Administrator access is reserved for the configured journal account.</p>
    </section>
</main>
<%@ include file="footer.jspf" %>
</body>
</html>
