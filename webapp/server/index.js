/* eslint consistent-return:0 */

const express = require('express')
const logger = require('./logger')

const argv = require('./argv')
const port = require('./port')
const setup = require('./middlewares/frontendMiddleware')
const { resolve } = require('path')
const proxy = require('http-proxy-middleware');
const app = express()

// If you need a backend, e.g. an API, add your custom backend-specific middleware here
// app.use('/api', myApi);

// get the intended host and port number, use localhost and port 3000 if not provided
const customHost = argv.host || process.env.HOST
const host = customHost || null // Let http.Server use its default IPv6/4 host
const prettyHost = customHost || 'localhost'

// proxy 中间件的选择项
var options = {
  target: 'http://127.0.0.1:8088', // 目标服务器 host
  changeOrigin: true,               // 默认false，是否需要改变原始主机头为目标URL
  pathRewrite: {
      '^/api' : '/api/rest_s/v1/visualis',     // 重写请求，比如我们源访问的是api/old-path，那么请求会被解析为/api/new-path
      '^/restj' : '/api/rest_j/v1/visualis',     // 重写请求，比如我们源访问的是api/old-path，那么请求会被解析为/api/new-path
  },
  onProxyReq(proxyReq, req, res) {
    proxyReq.setHeader('cookie', 'bdp-user-ticket-id=rHO3MAlaaq4D5hUA68BCaizVjRJlSlmE9AyiXWG56Fo=;workspaceId=224');
  }
}
// 创建代理
var exampleProxy = proxy(options);

app.use('/api', exampleProxy);
app.use('/restj', exampleProxy);
app.use('/login', exampleProxy);

// In production we need to pass these values in instead of relying on webpack
setup(app, {
  outputPath: resolve(process.cwd(), 'build'),
  publicPath: '/'
})


// use the gzipped bundle
app.get('*.js', (req, res, next) => {
  req.url = req.url + '.gz' // eslint-disable-line
  res.set('Content-Encoding', 'gzip')
  next()
})

// Start your app.
app.listen(port, host, async err => {
  if (err) {
    return logger.error(err.message)
  }

  logger.appStarted(port, prettyHost)
})
