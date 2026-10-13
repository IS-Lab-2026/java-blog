<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Contact | Papertrail</title>
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
    <main class="info-layout contact-layout">
      <section class="info-intro">
        <span class="eyebrow">Reach out</span>
        <h1>We read every note.</h1>
        <p>
          Questions, ideas, feedback, or a story you think belongs here. Send it
          our way.
        </p>
        <div class="contact-aside">
          <span class="note-dot"></span
          ><span>Your message goes straight to the journal team.</span>
        </div>
      </section>
      <section class="contact-form-wrap">
        <c:if test="${param.sent eq '1'}"
          ><p class="form-success" role="status">
            Your note is in. Thanks for writing.
          </p></c:if
        >
        <form
          class="form-stack"
          method="post"
          action="<c:url value='/contact'/>"
        >
          <input
            type="hidden"
            name="csrf"
            value="<c:out value='${sessionScope.csrfToken}'/>"
          /><label
            >Your name<input
              name="name"
              autocomplete="name"
              maxlength="200"
              value="<c:out value='${sessionScope.userName}'/>"
              required /></label
          ><label
            >Email address<input
              name="email"
              type="email"
              autocomplete="email"
              maxlength="250"
              required /></label
          ><label
            >Subject<input name="subject" maxlength="250" required /></label
          ><label
            >Your message<textarea
              name="message"
              maxlength="5000"
              rows="7"
              required
            ></textarea></label
          ><button class="button" type="submit">
            Send your note <span aria-hidden="true">&rarr;</span>
          </button>
        </form>
      </section>
    </main>
    <%@ include file="footer.jspf" %>
  </body>
</html>
