<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>About | Papertrail</title>
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
    <main class="about-page">
      <section class="about-hero">
        <div>
          <span class="eyebrow">A little about us</span>
          <h1>There is room<br />for another point of view.</h1>
          <p>
            Papertrail is an independent journal built around good writing, open
            conversations, and the people brave enough to share what they
            notice.
          </p>
        </div>
        <img
          src="https://images.unsplash.com/photo-1519682337058-a94d519337bc?auto=format&fit=crop&w=1400&q=85"
          alt="Books and notes laid out across a table"
        />
      </section>
      <section class="about-principles">
        <article>
          <span class="principle-number">01</span>
          <h2>Read with care.</h2>
          <p>
            Make space for a range of voices and give every story the attention
            it deserves.
          </p>
        </article>
        <article>
          <span class="principle-number">02</span>
          <h2>Write with honesty.</h2>
          <p>
            Editors can publish their work, learn in public, and build a
            readership over time.
          </p>
        </article>
        <article>
          <span class="principle-number">03</span>
          <h2>Keep it human.</h2>
          <p>
            Readers can respond, leave thoughtful notes, and follow the writers
            who move them.
          </p>
        </article>
      </section>
      <section class="about-end">
        <span class="eyebrow">Come sit with us</span>
        <h2>The journal gets better with you in it.</h2>
        <a class="button" href="<c:url value='/register'/>"
          >Join Papertrail <span aria-hidden="true">&rarr;</span></a
        >
      </section>
    </main>
    <%@ include file="footer.jspf" %>
  </body>
</html>
