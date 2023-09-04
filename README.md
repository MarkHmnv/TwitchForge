# 🤖💬 TwitchForge Telegram Bot

This bot allows you to access VODs (Video on Demand) on Twitch, even those reserved for subscribers only. Additionally, it also recovers deleted VODs within a 60-day period.

## Pre-Requisites

Before you start using this bot, please install the [Native HLS Playback Extension](https://chrome.google.com/webstore/detail/native-hls-playback/emnphkkblegpebimobpbekeedfgemhof) on your Google Chrome browser.

## Commands

- `/start`: Start the bot.
- `/retrieve`: Retrieve a VOD with a given URL.
- `/recover`: Recover a deleted VOD with a given URL from the Twitch Tracker.

## Methods Description

- `retrieveVod`: Method to retrieve VODs, including those exclusive for subscribers.
- `recoverVod`: Method to recover deleted VODs within 60 days.

## Example Usage

After starting the bot, you can type `/retrieve` followed by the URL of the VOD you want to access. This also works for VODs that are subscriber exclusive.

If you have a URL from the Twitch Tracker for a VOD that has been deleted within the last 60 days, you can use the `/recover` command followed by the URL to recover it.