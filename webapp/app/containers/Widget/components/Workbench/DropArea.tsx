import * as React from 'react'
import * as classnames from 'classnames'
import {IDataParamSource, DragType} from './Dropbox'
const styles = require('./Workbench.less')

interface DropAreaProps {
  dragged: IDataParamSource
  type: DragType
  beforeDrop: (draggedItem: IDataParamSource) => void
}

interface DropAreaState {
  entering: boolean
}

export default class DropArea extends React.PureComponent<DropAreaProps, DropAreaState>{
  constructor(props) {
    super(props);
    this.state = {
      entering: false
    }
  }
  private container: HTMLDivElement = null
  private width: number = 0
  private x: number = 0
  private y: number = 0
  private getBoxRect = () => {
    const rect = this.container.getBoundingClientRect() as DOMRect
    this.width = rect.width
    this.x = rect.x
    this.y = rect.y
  }
  private dragEnter = () => {
    // this.getBoxRect();
    this.setState({
      entering: true
    })
  }
  private dragOver = (e) => {
    e.preventDefault()
    e.dataTransfer.dropEffect = "move"
  }
  private dragLeave = () => {
    this.setState({
      entering: false
    })
  }
  private drop = (e) => {
    const { beforeDrop, dragged } = this.props
    e.dataTransfer.dropEffect = "move"
    beforeDrop(dragged);
    this.setState({
      entering: true
    })
  }
  render() {
    let {dragged, type} = this.props;
    let {entering} = this.state;
    let shouldResponse = false
    let shouleEnter = false
    let dragType = ''
    if (dragged) {
      dragType = dragged.type
      if (type === dragType) {
        shouldResponse = true
        shouleEnter = entering
      }
    }
    const containerClass = classnames({
      [styles.dropArea]: true,
      [styles.dragOver]: shouldResponse
    })
    const maskClass = classnames({
      [styles.mask]: true,
      [styles.onTop]: shouldResponse,
      [styles.enter]: shouleEnter,
      [styles.category]: dragType === 'category',
      [styles.value]: dragType === 'value'
    })
    return (
      <div ref={(f) => this.container = f} className={containerClass}>
        <div className={maskClass}
          onDragEnter={this.dragEnter}
          onDragOver={this.dragOver}
          onDragLeave={this.dragLeave}
          onDrop={this.drop}>
        </div>
      </div>

    )
  }
}