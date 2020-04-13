import React, {Component} from 'react';
import {observer, inject} from 'mobx-react';

import classes from './style.css';
import Config from '../../../config';
import jrQRCode from 'jr-qrcode'
import wfc from '../../../wfc/client/wfc'
import PCSession from '../../../wfc/model/pcsession';
import {observable} from 'mobx';
import axios from 'axios';
import {connect} from '../../../platform'

@inject(stores => ({
    avatar: stores.sessions.avatar,
    code: stores.sessions.code,
}))
@observer
export default class Login extends Component {
    @observable qrCode;
    token = '';
    loginTimer;
    qrCodeTimer;

    lastToken;

    componentDidMount() {
        axios.defaults.baseURL = Config.APP_SERVER;

        this.getCode();
        this.keepLogin();
        this.refreshQrCode();
    }

    componentWillUnmount() {
        console.log('login will disappear');
        clearInterval(this.loginTimer);
        clearInterval(this.qrCodeTimer);
    }

    renderUser() {
        return (
            <div className={classes.inner}>
                {
                    <img
                        className="disabledDrag"
                        src={this.props.avatar}/>
                }

                <p>Scan successful</p>
                <p>Confirm login on mobile WildfireChat</p>
            </div>
        );
    }

    async getCode() {
        var response = await axios.post('/pc_session', {
            token: this.token,
            device_name: 'web',
            clientId: wfc.getClientId(),
            platform: Config.getWFCPlatform()
        });
        console.log('----------- getCode', response.data);
        if (response.data) {
            let session = Object.assign(new PCSession(), response.data.result);
            this.token = session.token;
            this.qrCode = jrQRCode.getQrBase64(Config.QR_CODE_PREFIX_PC_SESSION + session.token);
        }
    }

    async keepLogin() {
        this.loginTimer = setInterval(() => {
            this.login();
        }, 1 * 1000);
    }

    async refreshQrCode() {
        this.qrCodeTimer = setInterval(() => {
            this.token = '';
            this.getCode();
        }, 30 * 1000);
    }

    async login() {
        if (this.token === '' || this.lastToken === this.token) {
            console.log('-------- token is empty or invalid');
            return;
        }
        var response = await axios.post('/session_login/' + this.token);
        console.log('---------- login', response.data);
        if (response.data) {
            switch (response.data.code) {
                case 0:
                    this.lastToken = this.token;
                    let userId = response.data.result.userId;
                    let token = response.data.result.token;
                    connect(userId, token);
                    break;
                default:
                    this.lastToken = '';
                    console.log(response.data);
                    break
            }
        }
    }

    renderCode() {

        return (
            <div className={classes.inner}>
                {
                    this.qrCode && (<img className="disabledDrag" src={this.qrCode}/>)
                }

                <a href={window.location.pathname + '?' + +new Date()}>刷新二维码</a>

                <p>扫码登录野火IM</p>
            </div>
        );
    }

    render() {
        return (
            <div className={classes.container}>
                {
                    // this.props.avatar ? this.renderUser() : this.renderCode()
                    this.renderCode()
                }
            </div>
        );
    }
}
