import React from 'react'

interface IHeadlessBrowserIdentifierProps {
  renderSign: boolean
  WidgetExecuteFailedTag: boolean
  parentNode: HTMLElement
}

function HeadlessBrowserIdentifier (props: IHeadlessBrowserIdentifierProps) {
  if (!props.renderSign) {
    if (props.WidgetExecuteFailedTag) {
      return (
        <>
          <input id="WidgetExecuteFailedTag" type="hidden" />
        </>
      )
    }
    return (
      <span />
    )
  } else {
    let offsetWidth, offsetHeight
    if (props.parentNode) {
      offsetWidth = props.parentNode.offsetWidth
      offsetHeight = props.parentNode.offsetHeight
    }
    if (props.WidgetExecuteFailedTag) {
      return (
        <>
          <input id="headlessBrowserRenderSign" type="hidden" />
          <input id="WidgetExecuteFailedTag" type="hidden" />
          <input id="width" type="hidden" value={offsetWidth} />
          <input id="height" type="hidden" value={offsetHeight} />
        </>
      )
    }
    return (
      <>
        <input id="headlessBrowserRenderSign" type="hidden" />
        <input id="width" type="hidden" value={offsetWidth} />
        <input id="height" type="hidden" value={offsetHeight} />
      </>
    )
  }
}


export default HeadlessBrowserIdentifier
