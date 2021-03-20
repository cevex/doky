const path = require('path');
const nodeExternals = require('webpack-node-externals');
const {CleanWebpackPlugin} = require('clean-webpack-plugin');
const webpack = require('webpack');

module.exports = {
    entry: './src/index.ts',
    module: {
        rules: [
            {
                test: /\.ts?$/,
                use: [
                    {loader: 'shebang-loader'},
                    {loader: 'ts-loader'}
                ],
                exclude: /node_modules/,
            },
        ],
    },
    node: {// Need this when working with express, otherwise the build fails
        __dirname: false,   // if you don't put this is, __dirname
        __filename: false,  // and __filename return blank or /
    },
    externals: [nodeExternals()], // Need this to avoid error when working with Express
    resolve: {
        extensions: ['.ts', '.js'],
    },
    plugins: [
        new CleanWebpackPlugin(),
        new webpack.BannerPlugin({banner: "#!/usr/bin/env node", raw: true}),
    ],
    output: {
        filename: 'cli.js',
        path: path.resolve(__dirname, 'bin'),
    },
};
