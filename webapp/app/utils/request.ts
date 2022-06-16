/*
 * <<
 * Davinci
 * ==
 * Copyright (C) 2016 - 2017 EDP
 * ==
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * >>
 */

import axios, { AxiosRequestConfig, AxiosResponse, AxiosPromise } from 'axios'

axios.defaults.validateStatus = function (status) {
  return status < 400
}

// 设置全局超时时间为60min
axios.defaults.timeout = 1000 * 60 * 60

function parseJSON (response: AxiosResponse) {
  return response.data
}

function refreshToken (response: AxiosResponse) {
  const token = response.data.header && response.data.header.token
  if (token) {
    setToken(token)
  }
  return response
}

export function request (config: AxiosRequestConfig): AxiosPromise
export function request (url: string, options?: AxiosRequestConfig): AxiosPromise
export default function request (url: any, options?: AxiosRequestConfig): AxiosPromise {

   // 1. 打印相关请求信息，方便调试
   console.log("urlStr:" + url + " urlObj:" + JSON.stringify(url))
   console.log("optionsStr:" + options + " optionsObj:" + JSON.stringify(options))

   // 2. 获取当前页面url
   var currentUrl = window.location.href
   console.log("当前页面的url为: " + currentUrl)

   // 3. labelValue默认带访问开发中心的标签
   var labelValue = "dev"

   // 4. 如果出现打开页面带有env=prod标签，表示访问生产中心
   //    并设置到sessionStorage里面存储
   if(typeof url === 'string' && url) {
      console.log("step-1")
     if(currentUrl.indexOf('env=prod') !== -1) {
       console.log("step-2")
       sessionStorage.setItem("env", "prod")
       console.log("进入生产中心！")
     }
   }

   // 5. 如果出现sessionStorage里面缓存有prod标签，设置labelValue为prod
   if(sessionStorage) {
     console.log("step-3")
     var env = sessionStorage.getItem("env")
     if(env && env === 'prod') {
        console.log("step-4")
        console.log("访问生产中心，所有的请求都带上了prod标签")
        labelValue = "prod"
     }
   }

   // 6. 通过判断不同请求类型，设置标签参数
   if (typeof url === 'string' && url) {
     console.log("step-5")
     if (options && typeof options === 'object') {
        if (options.method === 'get') {
          console.log("step-6")
          url = addQueryStringParameter(url, 'labelsRoute', labelValue)
        } else if (options.method === 'post' || options.method === 'put') {
            console.log("step-7")
            // 特殊情况，当option.data为数组时，POST请求的label参数加到url中
            if(Array.isArray(options.data)) {
              console.log("step-7.1")
              url = addQueryStringParameter(url, 'labelsRoute', labelValue)
            } else {
              options.data.labels = {route: labelValue}
            }
        } else if (options.method === 'delete') {
             console.log("step-8")
            if(!options.data) {
              console.log("step-9")
              options.data = {}
              options.data.labels = {route: labelValue}
            } else {
              console.log("step-10")

              options.data = {slides: options.data}
              options.data.labels = {route: labelValue}
            }
        }
     } else {
       console.log("step-11")
       url = addQueryStringParameter(url, 'labelsRoute', labelValue)
     }
   } else if (typeof url === 'object' && url) {
     console.log("step-12")
     if (url.method === 'get') {
       console.log("step-13")
       url.url = addQueryStringParameter(url.url, 'labelsRoute', labelValue)
     } else if (url.method === 'post' || url.method === 'put') {
       console.log("step-14")
       // 特殊情况，当url.data为数组时，POST请求的label参数加到url中
       if(Array.isArray(url.data)) {
          console.log("step-7.1")
          url.url = addQueryStringParameter(url.url, 'labelsRoute', labelValue)
       } else {
          url.data.labels = {route: labelValue}
       }
     } else if (url.method === 'delete') {
       console.log("step-15")
       if(!url.data) {
           console.log("step-16")
           url.data = {}
           url.data.labels = {route: labelValue}
        } else {
           console.log("step-17")
           url.data.labels = {route: labelValue}
        }
     }
   }
  return axios(url, options)
    .then(refreshToken)
    .then(parseJSON)
}

function addQueryStringParameter(uri, key, value) {
    if(!value) {
        return uri
    }
    var separator = uri.indexOf('?') !== -1 ? "&" : "?"
    console.log("URL添加Label标签后为：" + uri + separator + key + "=" + value)
    return uri + separator + key + "=" + value
}

export function setToken (token: string) {
  window.addEventListener('storage', syncToken, false)
  localStorage.setItem('TOKEN', token)
  localStorage.setItem('TOKEN_EXPIRE', `${new Date().getTime() + 3600000}`)
  axios.defaults.headers.common['Authorization'] = `Bearer ${token}`
}

function syncToken (e: StorageEvent) {
  const { key, newValue } = e
  if (key !== 'TOKEN') { return }
  if (!newValue) {
    delete axios.defaults.headers.common['Authorization']
  } else {
    axios.defaults.headers.common['Authorization'] = `Bearer ${newValue}`
  }
}

export function removeToken () {
  window.addEventListener('storage', syncToken)
  localStorage.removeItem('TOKEN')
  localStorage.removeItem('TOKEN_EXPIRE')
  delete axios.defaults.headers.common['Authorization']

}

export function getToken () {
  return axios.defaults.headers.common['Authorization']
}

interface IDavinciResponseHeader {
  code: number
  msg: string
  token: string
}

export interface IDavinciResponse<T> {
  header: IDavinciResponseHeader,
  payload: T
}
