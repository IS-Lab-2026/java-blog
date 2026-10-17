<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Editors you follow | Papertrail</title>
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
    <main class="workspace-shell">
      <div class="workspace-heading">
        <div>
          <span class="eyebrow">Your reading list</span>
          <h1>Editors you follow.</h1>
          <p>Keep up with the writers whose stories you enjoy.</p>
        </div>
        <span class="count-pill"
          ><c:out value="${editors.size()}" /> followed</span
        >
      </div>
      <section class="admin-section">
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Editor</th>
                <th>Following since</th>
                <th>Published stories</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="editor" items="${editors}"
                ><tr>
                  <td>
                    <strong><c:out value="${editor.user_name}" /></strong>
                  </td>
                  <td><c:out value="${editor.subscription_date}" /></td>
                  <td><c:out value="${editor.story_count}" /></td></tr
              ></c:forEach>
              <c:if test="${empty editors}"
                ><tr>
                  <td colspan="3" class="table-empty">
                    You are not following any editors yet. Find a story you like
                    and follow its author.
                  </td>
                </tr></c:if
              >
            </tbody>
          </table>
        </div>
      </section>
    </main>
    <%@ include file="footer.jspf" %>
  </body>
</html>
