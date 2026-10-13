<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title><c:out value="${post.title}"/> | Papertrail</title>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
      href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Newsreader:opsz,wght@6..72,400;6..72,500;6..72,600&display=swap"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="<c:url value='/assets/css/site.css'/>" />
  </head>
  <body>
    <%@ include file="header.jspf" %>
    <main class="reading-shell">
      <a class="back-link" href="<c:url value='/posts'/>">&larr; All stories</a>
      <article class="story-article">
        <div class="story-topline">
          <span class="eyebrow">The journal</span
          ><span class="story-date"><c:out value="${post.createdAt}" /></span>
        </div>
        <h1 class="story-title"><c:out value="${post.title}" /></h1>
        <p class="story-deck"><c:out value="${post.blogDescription}" /></p>
        <div class="story-byline">
          <span class="avatar avatar-small"
            ><c:out value="${post.authorName}" /></span
          ><span
            >By
            <strong
              ><c:out
                value="${not empty post.authorName ? post.authorName : 'Papertrail editor'}" /></strong></span
          ><span class="story-stats"
            ><c:out value="${post.views}" /> reads &middot;
            <c:out value="${post.likes}" /> likes</span
          >
        </div>
        <div class="story-rule"></div>
        <div class="story-body"><c:out value="${post.body}" /></div>
        <section class="story-actions" id="story-actions">
          <div>
            <span class="eyebrow">Enjoyed this story?</span>
            <p>Pass a little encouragement along.</p>
          </div>
          <div class="action-row">
            <c:choose>
              <c:when test="${not empty sessionScope.userId}">
                <form method="post" action="<c:url value='/posts'/>">
                  <input
                    type="hidden"
                    name="csrf"
                    value="<c:out value='${sessionScope.csrfToken}'/>"
                  /><input type="hidden" name="action" value="like" /><input
                    type="hidden"
                    name="blogId"
                    value="<c:out value='${post.blogId}'/>"
                  />
                  <button
                    class="button <c:if test='${liked}'>button-accent</c:if>"
                    type="submit"
                  >
                    <c:choose
                      ><c:when test="${liked}">Liked</c:when
                      ><c:otherwise>Like story</c:otherwise></c:choose
                    >
                    &middot; <c:out value="${post.likes}" />
                  </button>
                </form>
                <c:if
                  test="${not empty post.authorId and post.authorId ne sessionScope.userId}"
                >
                  <form method="post" action="<c:url value='/posts'/>">
                    <input
                      type="hidden"
                      name="csrf"
                      value="<c:out value='${sessionScope.csrfToken}'/>"
                    /><input
                      type="hidden"
                      name="action"
                      value="subscribe"
                    /><input
                      type="hidden"
                      name="blogId"
                      value="<c:out value='${post.blogId}'/>"
                    />
                    <button class="button button-outline" type="submit">
                      <c:choose
                        ><c:when test="${following}">Following author</c:when
                        ><c:otherwise>Follow author</c:otherwise></c:choose
                      >
                    </button>
                  </form>
                </c:if>
              </c:when>
              <c:otherwise
                ><a class="button" href="<c:url value='/login'/>"
                  >Sign in to like or follow</a
                ></c:otherwise
              >
            </c:choose>
          </div>
        </section>
        <section class="comments-section" id="comments">
          <div class="section-heading">
            <div>
              <span class="eyebrow">Conversation</span>
              <h2>
                Reader notes
                <span class="count-pill"
                  ><c:out value="${commentsList.size()}"
                /></span>
              </h2>
            </div>
          </div>
          <c:choose>
            <c:when test="${not empty sessionScope.userId}">
              <form
                class="comment-form"
                method="post"
                action="<c:url value='/posts'/>"
              >
                <input
                  type="hidden"
                  name="csrf"
                  value="<c:out value='${sessionScope.csrfToken}'/>"
                /><input type="hidden" name="action" value="comment" /><input
                  type="hidden"
                  name="blogId"
                  value="<c:out value='${post.blogId}'/>"
                />
                <label class="visually-hidden" for="comment"
                  >Add to the conversation</label
                ><textarea
                  id="comment"
                  name="comment"
                  maxlength="3000"
                  rows="3"
                  placeholder="Add a thoughtful note..."
                  required
                ></textarea>
                <button class="button button-small" type="submit">
                  Post comment
                </button>
              </form>
            </c:when>
            <c:otherwise
              ><p class="empty-note">
                Sign in to join the conversation.
                <a href="<c:url value='/login'/>">Sign in</a>
              </p></c:otherwise
            >
          </c:choose>
          <div class="comment-list">
            <c:forEach var="item" items="${commentsList}">
              <article class="comment-item">
                <div class="comment-meta">
                  <strong><c:out value="${item.authorName}" /></strong
                  ><time><c:out value="${item.createdAt}" /></time>
                </div>
                <p><c:out value="${item.body}" /></p>
              </article>
            </c:forEach>
            <c:if test="${empty commentsList}"
              ><p class="empty-note">
                No notes yet. Start the conversation.
              </p></c:if
            >
          </div>
        </section>
      </article>
    </main>
    <%@ include file="footer.jspf" %>
  </body>
</html>
