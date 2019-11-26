/**
 * RoutePage
 *
 * This is the page we show when the user visits a url that doesn't have a route
 */

import React from 'react'

export default function NoAuthorization () {
  // 有 # 时，#后的内容都会被放到hash中，不会被window.location.search拿到
  const hash = window.location.hash
  let querys = hash.split('?')
  let proxyUser = ''
  let validationCode = ''
  if (querys.length > 1) {
    // 说明有?后的的内容
    querys = querys[1].split('&')
    proxyUser = querys[0].split('=')[1]
    validationCode = querys[1].split('=')[1]
  }

  fetch(`http://test.com/api/rest_j/v1/user/proxy?proxyUser=${proxyUser}&validationCode=${validationCode}`, {method: 'get'})

  return (
    <div style={{width: '400px', fontSize: '18px', margin: '200px auto 0 auto', textAlign: 'center'}}>
      正在确认知画(Drawis)系统登录状态...
    </div>
  )
}
