package org.fao.geonet.services.reports;

import jeeves.constants.Jeeves;
import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.Util;
import org.fao.geonet.domain.*;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.OperationAllowedSpecs;
import org.jdom.Element;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Service to return the updated metadata during a period.
 *
 * Service parameters:
 *   dateFrom (mandatory)
 *   dateTo   (mandatory)
 *   groups   (optional)
 *
 * Service output:
 * <p/>
 * <response>
 * <record>
 * <username></username>                    Owner username
 * <surname></surname>                      Owner surname
 * <name></name>                            Owner name
 * <email></email>                          Owner mail
 * <groupName></groupName>                  Group owner name
 * <groupOwnerMail></groupOwnerMail>        Group owner mail
 * <recordName></recordName>                Metadata title
 * <uuid></uuid>                            Metadata UUID
 * <changedate></changedate>                Metadata change date
 * </record>
 * </response>
 *
 * @author Jose García
 */
public class ReportInternalMetadata implements Service {
    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    // --------------------------------------------------------------------------
    // ---
    // --- Service
    // ---
    // --------------------------------------------------------------------------

    public Element exec(Element params, ServiceContext context)
            throws Exception {

        // Process parameters
        String beginDate = Util.getParam(params, "dateFrom");
        String endDate = Util.getParam(params, "dateTo");

        beginDate = beginDate + "T00:00:00";
        endDate = endDate + "T23:59:59";

        ISODate beginDateIso = new ISODate(beginDate);
        ISODate endDateIso = new ISODate(endDate);
        Set<Integer> groupList = ReportUtils.groupsForFilter(context, params);

        // Retrieve metadata
        //final Sort sort = new Sort(Sort.Direction.DESC, SortUtils.createPath(Metadata_.dataInfo, MetadataDataInfo_.changeDate));
        final List<Metadata> records = context.getBean(MetadataRepository.class).getMetadataReports().
                getInternalMetadata(beginDateIso, endDateIso, groupList, OperationAllowedSpecs.isPublic(ReservedOperation.view));

        // Process metadata results for the report
        Element response = new Element(Jeeves.Elem.RESPONSE);

        // Process the records
        for (Metadata metadata : records) {
            User userOwner = context.getBean(UserRepository.class).findOne(metadata.getSourceInfo().getOwner());
            Group groupOwner = context.getBean(GroupRepository.class).findOne(metadata.getSourceInfo().getGroupOwner());

            String userOwnerUsername= userOwner.getUsername();
            String userOwnerName= (userOwner.getName() != null?userOwner.getName():"");
            String userOwnerSurname= (userOwner.getSurname() != null?userOwner.getSurname():"");
            String userOwnerMail = (userOwner.getEmail() != null?userOwner.getEmail():"");

            String groupOwnerName = (groupOwner.getLabelTranslations().get(context.getLanguage()) != null?
                    groupOwner.getLabelTranslations().get(context.getLanguage()): groupOwner.getName());
            String groupOwnerMail = (groupOwner.getEmail() != null?groupOwner.getEmail():"");
            String mdTitle = ReportUtils.retrieveMetadataTitle(context, metadata.getId());


            // Build the record element with the information for the report
            Element metadataEl = new Element("record");
            metadataEl.addContent(new Element("uuid").setText(metadata.getUuid()))
                    .addContent(new Element("recordName").setText("" + mdTitle))
                    .addContent(new Element("createdate").setText("" + metadata.getDataInfo().getCreateDate()))
                    .addContent(new Element("username").setText(userOwnerUsername))
                    .addContent(new Element("surname").setText(userOwnerSurname))
                    .addContent(new Element("name").setText(userOwnerName))
                    .addContent(new Element("email").setText(userOwnerMail))
                    .addContent(new Element("groupName").setText(groupOwnerName))
                    .addContent(new Element("groupOwnerMail").setText(groupOwnerMail));

            response.addContent(metadataEl);
        }

        return response;
    }


}