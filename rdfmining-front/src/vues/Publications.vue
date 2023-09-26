<template>
    <div class="container">
        <!-- Specific search by key -->
        <label style="font-size: 1vw;">Filtering results: </label>
        <select style="font-size: 1vw;" v-model="choosenFilter">
            <option value="">--Please choose an option--</option>
            <option value="date_asc">By date (asc)</option>
            <option value="date_desc">By date (desc)</option>
            <option value="title">By title</option>
            <option value="conferenceTitle">By conference</option>
            <option value="place">By place</option>
        </select>
        <div v-for="paper in sortByFilter(choosenFilter)" :key="paper">
            <CCard>
                <CCardBody>
                    <!-- <CCardHeader>PAPER TYPE</CCardHeader> -->
                    <CCardTitle>{{ paper.title }}</CCardTitle>
                    <CCardSubtitle class="mb-2 text-muted">
                        <template v-for="(author, idx) in paper.authors" :key="idx">
                            {{ author }} <template v-if="idx != paper.authors.length - 1"> and </template>
                        </template>
                    </CCardSubtitle>
                    <CCardSubtitle class="mb-2 text-muted">{{ paper.conferenceTitle }} ({{ paper.date }})</CCardSubtitle>
                    <CCardSubtitle v-if="paper.distinction.link != ''">
                        <a :href="paper.distinction.link">
                            {{ paper.distinction.type }}
                        </a>
                    </CCardSubtitle>
                    <CCardText><b>Abstract: </b>{{ paper.abstract }}</CCardText>
                    <CCardText>
                        <b>Keywords: </b>
                        <template v-for="(keyword, idx) in paper.keywords" :key="idx">
                            {{ keyword }} <template v-if="idx != paper.keywords.length - 1"> ; </template>
                        </template>
                    </CCardText>
                    <CCardLink @click="copyCitation(paper.citation)">Copy Citation</CCardLink>
                    <CCardLink :href="paper.link">Access to the ressource</CCardLink>
                </CCardBody>
            </CCard>
            <br>
        </div>

    </div>
</template>


<script>
// import { publications } from '../data/publications.json'
import _ from 'lodash';
import { get } from '@/tools/api';
import { CCard, CCardBody, CCardText, CCardTitle, CCardSubtitle, CCardLink } from '@coreui/vue'
// import { useCookies } from 'vue3-cookies'

export default {
    name: 'RDFMinerPublications',
    components: {
        CCard, CCardBody, CCardText, CCardTitle, CCardSubtitle, CCardLink
    },
    data() {
        return {
            papers: [],
            keywords: [],
            choosenFilter: "",
        }
    },
    mounted() {
        // 
        // const cookies = useCookies(["token", "id"]).cookies;
        // console.log(cookies.get("token"));
        // build a request to the API
        // axios.get("api/publications").then(
        //     (response) => {
        //         if (response.status === 200) {
        //             // fill papers list
        //             response.data.forEach((paper) => {
        //                 this.papers.push(paper);
        //                 // console.log(paper)
        //             })
        //         }
        //     }
        // ).catch((error) => {
        //     console.log(error);
        // });
        this.getPublications();
    },
    methods: {
        async getPublications() {
            // get project
            const publications = await get("api/publications", {});
            // console.log(project);
            this.papers = publications;
        },
        async copyCitation(citation) {
            try {
                await navigator.clipboard.writeText(citation);
                alert('Copied');
            } catch ($e) {
                alert('Cannot copy');
            }
        },
        sortByFilter(key) {
            if (key === "date_asc") {
                return _.orderBy(this.papers, 'date');
            } else if (key === "date_desc") {
                return _.orderBy(this.papers, 'date', 'desc');
            } else {
                return _.orderBy(this.papers, key);
            }
        }
    }
}
</script>

<!-- Add "scoped" attribute to limit CSS to this component only -->
<style scoped>
@import url(https://fonts.googleapis.com/css?family=Merriweather:400,300,700);

@import url(https://fonts.googleapis.com/css?family=Montserrat:400,700);

.keyword {
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
    display: inline-block;
    background-color: rgb(0, 63, 127);
    color: #ffffff;
    padding: 6px 12px;
    border-radius: 20px;
    margin-right: 10px;
    margin-bottom: 30px;
    font-size: 1vw;
    text-align: center;
}

a.button {
    box-shadow: 0 4px 8px 0 rgba(0, 0, 0, 0.2);
    display: inline-block;
    background-color: #a5a5a5;
    color: #ffffff;
    padding: 6px 12px;
    border-radius: 2px;
    margin-right: 10px;
    margin-bottom: 30px;
    font-size: 1vw;
    text-align: center;
    cursor: pointer;
    text-decoration: none;
}

body {
    background: #fbfbfb;
    font-family: 'Merriweather', serif;
    font-size: 22vw;
    color: #777;
}

h1 {
    font-family: 'Montserrat', sans-serif;
    font-size: 2vw;
}

h4 {
    font-family: 'Montserrat', sans-serif;
    font-size: 1vw;
}

.row {
    padding: 50px 0;
}

.seperator {
    margin-bottom: 30px;
    width: 35px;
    height: 3px;
    background: #777;
    border: none;
}

.title {
    text-align: center;

    .row {
        padding: 50px 0 0;
    }

    h1 {
        text-transform: uppercase;
    }

    .seperator {
        margin: 0 auto 10px;
    }
}

.item {
    position: relative;
    margin-bottom: 30px;
    min-height: 1px;
    float: left;
    -webkit-backface-visibility: hidden;
    -webkit-tap-highlight-color: transparent;
    -webkit-touch-callout: none;
    -webkit-user-select: none;
    -moz-user-select: none;
    -ms-user-select: none;
    user-select: none;
    font-size: 1vw;

    .item-in {
        background: #fff;
        padding: 40px;
        position: relative;

        &:hover:before {
            width: 100%;
        }

        &::before {
            content: "";
            position: absolute;
            bottom: 0px;
            height: 2px;
            width: 0%;
            background: #333333;
            right: 0px;
            -webkit-transition: width 0.4s;
            transition: width 0.4s;
        }
    }

}

.item {

    h4 {
        font-size: 18vw;
        margin-top: 25px;
        letter-spacing: 2px;
        text-transform: uppercase;
    }

    p {
        font-size: 12vw;
    }

    a {
        font-family: 'Montserrat', sans-serif;
        font-size: 12px;
        text-transform: uppercase;
        color: #666666;
        margin-top: 10px;

        i {
            opacity: 0;
            padding-left: 0px;
            transition: 0.4s;
            font-size: 24px;
            display: inline-block;
            top: 5px;
            position: relative;
        }

        &:hover {
            text-decoration: none;

            i {
                padding-left: 10px;
                opacity: 1;
                font-weight: 300;
            }
        }
    }
}

.item .icon {
    position: absolute;
    top: 27px;
    left: -16px;
    cursor: pointer;

    a {
        font-family: 'Merriweather', serif;
        font-size: 14px;
        font-weight: 400;
        color: #999;
        text-transform: none;
    }

    svg {
        width: 32px;
        height: 32px;
        float: left;
    }

    .icon-topic {
        opacity: 0;
        padding-left: 0px;
        transition: 0.4s;
        display: inline-block;
        top: 0px;
        position: relative;
    }

    &:hover .icon-topic {
        opacity: 1;
        padding-left: 10px;
    }
}

@media only screen and (max-width : 768px) {
    .item .icon {
        position: relative;
        top: 0;
        left: 0;
    }
}
</style>