<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
uri="jakarta.tags.core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Papertrail | Stories worth staying for</title>
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
      href="https://fonts.googleapis.com/css2?family=DM+Sans:wght@400;500;600;700&family=Newsreader:opsz,wght@6..72,400;6..72,500;6..72,600&display=swap"
      rel="stylesheet"
    />
    <link rel="stylesheet" href="<c:url value='/assets/css/site.css'/>" />
  </head>
  <body>
    <%@ include file="/WEB-INF/views/header.jspf" %>
    <main>
      <section class="home-hero">
        <div class="hero-copy">
          <span class="eyebrow">A journal for curious minds</span>
          <h1>
            Stories worth<br />staying for<span class="brand-dot">.</span>
          </h1>
          <p>
            Thoughtful words from people who notice the little things, ask
            better questions, and keep learning out loud.
          </p>
          <a class="button" href="#latest"
            >Find your next read <span aria-hidden="true">&darr;</span></a
          >
        </div>
        <div class="hero-image">
          <img
            src="https://images.unsplash.com/photo-1455390582262-044cdead277a?auto=format&fit=crop&w=1500&q=85"
            alt="Notebook and pen ready for a new story"
          />
          <div class="image-note">
            <span class="note-dot"></span> Independent voices, shared here
          </div>
        </div>
        <div class="hero-index" aria-hidden="true">VOL. 01<br />EST. TODAY</div>
      </section>
      <section class="latest-section" id="latest">
        <div class="section-heading">
          <div>
            <span class="eyebrow">The latest</span>
            <h2>Fresh from the page</h2>
          </div>
          <span class="section-aside">Read slowly. Stay curious.</span>
        </div>
        <c:choose>
          <c:when test="${not empty posts}">
            <div class="story-grid">
              <c:forEach var="post" items="${posts}">
                <article class="story-card">
                  <div class="card-topline">
                    <span class="eyebrow">Field notes</span
                    ><span class="card-date"
                      ><c:out value="${post.createdAt}"
                    /></span>
                  </div>
                  <h3>
                    <a
                      href="<c:url value='/post'><c:param name='slug' value='${post.slug}'/></c:url>"
                      ><c:out value="${post.title}"
                    /></a>
                  </h3>
                  <p class="card-description">
                    <c:out value="${post.blogDescription}" />
                  </p>
                  <div class="card-footer">
                    <span
                      >By
                      <strong
                        ><c:out
                          value="${not empty post.authorName ? post.authorName : 'Papertrail editor'}" /></strong></span
                    ><span><c:out value="${post.views}" /> reads</span>
                  </div>
                  <a
                    class="card-read"
                    href="<c:url value='/post'><c:param name='slug' value='${post.slug}'/></c:url>"
                    aria-label="Read <c:out value='${post.title}'/>"
                    >Read story <span aria-hidden="true">&rarr;</span></a
                  >
                </article>
              </c:forEach>
            </div>
          </c:when>
          <c:otherwise
            ><div class="empty-stories">
              <span class="eyebrow">A blank page, for now</span>
              <h3>The next good story starts with someone like you.</h3>
              <p>Editors can bring the first story to life from the studio.</p>
              <c:if
                test="${sessionScope.role eq 'editor' or sessionScope.role eq 'admin'}"
                ><a class="text-link" href="<c:url value='/editor'/>"
                  >Open editor studio &rarr;</a
                ></c:if
              >
            </div></c:otherwise
          >
        </c:choose>
      </section>
      <section class="join-strip">
        <div>
          <span class="eyebrow eyebrow-light">Find your people</span>
          <h2>Good stories travel further together.</h2>
          <p>
            Follow editors you love and get a note when they publish something
            new.
          </p>
        </div>
        <c:choose
          ><c:when test="${empty sessionScope.userId}"
            ><a class="button button-paper" href="<c:url value='/register'/>"
              >Join the journal <span aria-hidden="true">&rarr;</span></a
            ></c:when
          ><c:otherwise
            ><a
              class="button button-paper"
              href="<c:url value='/notifications'/>"
              >Your notifications <span aria-hidden="true">&rarr;</span></a
            ></c:otherwise
          ></c:choose
        >
      </section>
    </main>
    <%@ include file="/WEB-INF/views/footer.jspf" %>
  </body>
</html>
