// sw.js
self.addEventListener('fetch', event => {
  // Only intercept top-level page navigations
  if (event.request.mode !== 'navigate') return;

  const url = new URL(event.request.url);

  // Only act on your host, and skip the problem page (and assets)
  const isBeta = url.hostname === 'beta.boblox.ca';
  const isProblem = url.pathname === '/problem' || url.pathname.startsWith('/problem/');
  const hasBypass = url.searchParams.has('noredirect'); // optional bypass ?noredirect=1

  if (isBeta && !isProblem && !hasBypass) {
    event.respondWith(Response.redirect('https://beta.boblox.ca/problem', 302));
  }
});
