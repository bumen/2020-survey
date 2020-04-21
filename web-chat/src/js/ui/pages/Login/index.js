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
    userId;

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
            console.log('-------- login token is empty or invalid');
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

    async mobileLogin() {
        var isLogin = wfc.isLogin();
        if (isLogin) {
            console.log("mobile is login success.");
            return;
        }

        var response = await axios.post('/login', {
            mobile: Config.CLIENT_MOBILE,
            code: 66666,
            clientId: wfc.getClientId(),
            platform: Config.getWFCPlatform()
        });
        console.log('----------- mobile login ok', response.data);
        if (response.data) {
            switch (response.data.code) {
                case 0:
                    this.userId = response.data.result.userId;

                    alert("login success");
                    console.log("mobile login success",response.data);
                    break;
                default:
                    this.userId = '';
                    console.log("mobile login fail",response.data);

                    break
            }
        }
    }

    async scanPc() {
        if (this.token === '') {
            console.log('-------- scan pc token is empty or invalid');
            return;
        }
        var response = await axios.post('/scan_pc/' + this.token);
        console.log('---------- scan pc', response.data);
        if (response.data) {
            switch (response.data.code) {
                case 0:
                    alert("scan pc success");

                    console.log("scan pc success", response.data);
                    break;
                default:
                    console.log("scan pc fail", response.data);
                    break
            }
        }
    }

    async confirmPc() {
        if (!this.userId) {
            console.log('-------- confirm pc userId is empty or invalid');
            return;
        }
        if (this.token === '') {
            console.log('-------- confirm pc token is empty or invalid');
            return;
        }
        var response = await axios.post('/confirm_pc/', {
            user_id: this.userId,
            token: this.token
        });
        console.log('---------- confirm pc', response.data);
        if (response.data) {
            switch (response.data.code) {
                case 0:
                    alert("confirm pc success");

                    console.log("confirm pc success ", response.data);
                    break;
                default:
                    console.log("confirm pc fails", response.data);
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


                <a href="javascript:void(0);" onClick={e => this.mobileLogin()}>登录</a>&nbsp
                <a href="javascript:void(0);" onClick={e => this.scanPc()}>扫码</a>&nbsp
                <a href="javascript:void(0);" onClick={e => this.confirmPc()}>扫码确认</a>
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
