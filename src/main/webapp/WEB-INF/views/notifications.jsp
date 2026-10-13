<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Notifications | Papertrail</title>
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
    <main class="workspace-shell inbox-shell">
      <div class="workspace-heading">
        <div>
          <span class="eyebrow">Your inbox</span>
          <h1>Notes from the journal.</h1>
          <p>New stories and conversations, gathered in one place.</p>
        </div>
        <c:if test="${not empty notifications}"
          ><form method="post" action="<c:url value='/notifications'/>">
            <input
              type="hidden"
              name="csrf"
              value="<c:out value='${sessionScope.csrfToken}'/>"
            /><button class="button button-outline" type="submit">
              Mark all read
            </button>
          </form></c:if
        >
      </div>
      <div class="notification-list">
        <c:forEach var="item" items="${notifications}"
          ><article
            class="notification-item <c:if test='${not item.is_read}'>notification-unread</c:if>"
          >
            <span class="notification-mark" aria-hidden="true"></span>
            <div class="notification-copy">
              <div class="notification-title">
                <strong><c:out value="${item.title}" /></strong
                ><time><c:out value="${item.created_at}" /></time>
              </div>
              <p><c:out value="${item.content}" /></p>
              <c:if test="${not empty item.actor_name}"
                ><small>From <c:out value="${item.actor_name}" /></small
              ></c:if>
            </div>
            <c:if test="${not empty item.link}"
              ><a class="text-link" href="<c:out value='${item.link}'/>"
                >Open &rarr;</a
              ></c:if
            >
          </article></c:forEach
        >
        <c:if test="${empty notifications}"
          ><div class="empty-stories">
            <span class="eyebrow">All caught up</span>
            <h2>Your next note will find you here.</h2>
            <p>Follow an editor to hear when they publish.</p>
          </div></c:if
        >
      </div>
    </main>
    <%@ include file="footer.jspf" %>
  </body>
</html>
